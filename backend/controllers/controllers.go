package controllers

import (
	"backend/models"
	"backend/repo"
	"backend/util"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"os/exec"
	"path"
	"strconv"
	"strings"
	"time"
	"github.com/dgrijalva/jwt-go"
	"golang.org/x/crypto/bcrypt"
)

// Define different controllers here for the routes

// Simply replies with "Pong"
func PingHandler(w http.ResponseWriter, r *http.Request) {

	// reply in JSON
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(200)

	json.NewEncoder(w).Encode("Pong")
}

// Register
func RegisterHandler(w http.ResponseWriter, r *http.Request) {
	// user struct to hold the response
	user := models.User{}
	// decode JSON body and place into user struct
	json.NewDecoder(r.Body).Decode(&user)

	// check for errors
	errors := []models.Error{} // slice of error to hold each errors

	// check for empty fields
	if user.FirstName == "" {
		// create new error object and append to errors
		errors = append(errors, util.NewError("", "Invalid Attribute", "First name must not be empty"))
	}
	if user.LastName == "" {
		errors = append(errors, util.NewError("", "Invalid Attribute", "Last name must not be empty"))
	}
	if user.Email == "" {
		errors = append(errors, util.NewError("", "Invalid Attribute", "Email must not be empty"))
	} else {
		// check if email already exists
		_, err := repo.GetUserByEmail(user.Email)
		if err == nil {
			errors = append(errors, util.NewError("", "Invalid Attribute", "Email already exists"))
		}
	}
	if user.Password == "" {
		errors = append(errors, util.NewError("", "Invalid Attribute", "Password must not be empty"))
	}

	// if there are errors, respond with errors
	if len(errors) > 0 {
		// send errors as response
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)

		json.NewEncoder(w).Encode(models.Errors{Errors: errors}) // send errors as response
		return
	}

	// process request
	passwordHashed, _ := bcrypt.GenerateFromPassword([]byte(user.Password), 10) // hash password
	user.Password = string(passwordHashed)

	// insert into db
	lastInsertID, err := repo.InsertUser(user)
	if err != nil {
		log.Fatalln(err)
	}
	fmt.Println("New user successfully inserted into database. Last insert ID: " + strconv.Itoa(lastInsertID))
	// set response type and status code
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(200)

	json.NewEncoder(w).Encode(map[string]string{"status": "OK", "message": "registration successful"})
}

// user authentication
func AuthHandler(w http.ResponseWriter, r *http.Request) {
	var user models.User

	// decode request body
	json.NewDecoder(r.Body).Decode(&user)

	// retrieve email and password
	email := user.Email
	passwordMaybe := user.Password

	errors := []models.Error{}

	// check for empty fields
	if email == "" {
		errors = append(errors, util.NewError("", "Invalid Attribute", "Email must not be empty"))
	}

	if passwordMaybe == "" {
		errors = append(errors, util.NewError("", "Invalid Attribute", "Password must not be empty"))
	}

	// check for errors
	if len(errors) > 0 {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(401)

		json.NewEncoder(w).Encode(models.Errors{Errors: errors})
		return
	}

	// retrieve user (if any) by email
	user, err := repo.GetUserByEmail(email)
	if err != nil {
		// email doesn't exist
		errors = append(errors, util.NewError("", "Unauthorized user", "Invalid email or password"))
	} else {
		// email exists in the database, proceed with comparing password
		// compare the password provided by the user with the hashed password from the database
		err = bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(passwordMaybe))
		if err != nil {
			// passwords do not match
			errors = append(errors, util.NewError("", "Unauthorized user", "Invalid email or password"))
		}
	}

	// check for errors
	if len(errors) > 0 {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(401)

		json.NewEncoder(w).Encode(models.Errors{Errors: errors})
		return
	}

	// authentication successful

	user.Password = "" // don't send hashed password

	// generate JWT claims and token
	claims := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.StandardClaims{
		Issuer:    strconv.Itoa(user.ID),
		ExpiresAt: time.Now().Add(time.Hour * 24).Unix(), // token has an expiry of 1 day
	})

	token, err := claims.SignedString([]byte(os.Getenv("JWT_SECRET")))
	if err != nil {
		log.Fatalln(err)
	}

	// create cookie to store JWT token
	cookie := &http.Cookie{
		Name:     "jwt",
		Value:    token,
		Expires:  time.Now().Add(time.Hour * 24),
		HttpOnly: true,
	}
	// send cookie to user
	http.SetCookie(w, cookie)

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(200)

	json.NewEncoder(w).Encode(user)
}

// Logout
func LogoutHandler(w http.ResponseWriter, r *http.Request) {
	// delete jwt cookie
	cookie := &http.Cookie{
		Name:     "jwt",
		Value:    "",
		Expires:  time.Now().Add(-time.Hour), // set cookie expiry to past
		HttpOnly: true,
	}

	http.SetCookie(w, cookie)

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(200)
	json.NewEncoder(w).Encode(map[string]string{"msg:": "Logout Successful"})
}

// Retrieves nutritional info of a fruit from CalorieNinja
func getNutritionalInfo(fruitName string) models.FruitNutrition {
	// API endpoint of CalorieNinja + fruit name parameter passed by the client
	endpoint := "https://api.calorieninjas.com/v1/nutrition?query=" + fruitName

	// Make a http call to CalorieNinja API
	client := &http.Client{}
	req, _ := http.NewRequest("GET", endpoint, nil)
	// Pass API key in header. The API key is an environment variable set in the .env file
	req.Header.Set("X-Api-Key", os.Getenv("CALORIE_NINJA_API_KEY"))
	resp, _ := client.Do(req)

	defer resp.Body.Close()

	// Response payload
	// var payload map[string]interface{}
	fruitNutrition := models.FruitNutrition{}

	// Decode JSON response from the API call and decode into payload struct
	json.NewDecoder(resp.Body).Decode(&fruitNutrition)
	return fruitNutrition
}

// SearchHandler accepts an input image, predicts the type of fruit then returns fruit nutritional info
func SearchHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("File Upload Endpoint Hit")

	// Parse our multipart form, 10 << 20 specifies a maximum
	// upload of 10 MB files.
	r.ParseMultipartForm(10 << 20)

	// FormFile returns the first file for the given key `image`
	// it also returns the FileHeader so we can get the Filename,
	// the Header and the size of the file
	file, handler, err := r.FormFile("image")
	if err != nil {
		fmt.Println("Error Retrieving the File")
		fmt.Println(err)
		return
	}
	defer file.Close()
	fmt.Printf("Uploaded File: %+v\n", handler.Filename)
	// fmt.Printf("File Size: %+v\n", handler.Size)
	// fmt.Printf("MIME Header: %+v\n", handler.Header)

	// Create a temporary file within our temp-images directory that follows
	// a particular naming pattern
	tempFile, err := ioutil.TempFile("temp-images", "upload-*.png")
	if err != nil {
		fmt.Println(err)
	}
	defer tempFile.Close()

	// read all of the contents of our uploaded file into a
	// byte array
	fileBytes, err := ioutil.ReadAll(file)
	if err != nil {
		fmt.Println(err)
	}
	// write this byte array to our temporary file
	tempFile.Write(fileBytes)
	// return that we have successfully uploaded our file!

	// Dodgy way to execute Python script from Go (but it works!)
	// Update according to your environment
	python := path.Clean(strings.Join([]string{"C:\\", "Users", "Marck", "AppData", "Local", "Programs", "Python", "Python310", "python.exe"}, "\\"))
	script := path.Clean(strings.Join([]string{"C:\\", "Users", "Marck", "Desktop", "team-12-project", "machine_learning", "predict.py"}, "\\"))
	img_path := "\\" + tempFile.Name()

	// executes the python script using CMD and passes the path of uploaded image
	cmd := exec.Command(python, script, img_path)
	out, err := cmd.Output()
	if err != nil {
		log.Fatal(err)
	}

	// predicted fruit name from python script. 
	predictionStr := strings.Trim(string(out), "\r\n") // trim "\r\n" from string

	// get nutritional info from CalorieNinja based on predicted fruit name
	fruitNutritionInfo := getNutritionalInfo(predictionStr)

	// The model will always output a prediction but the predicted fruit may not exist in CalorieNinja
	// if this is the case, just return the name of the predicted fruit with a message.
	if len(fruitNutritionInfo.Items) == 0 {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)

		json.NewEncoder(w).Encode(map[string]string{"name:": predictionStr, "msg:": "Unable to find nutritional info."})

	} else {
		payload := models.Fruit{
			Name: predictionStr,
			Calories: 	fruitNutritionInfo.Items[0].Calories,
			Carbs: 		fruitNutritionInfo.Items[0].Carbs,
			Chols: 		fruitNutritionInfo.Items[0].Chols,
			FatSat: 	fruitNutritionInfo.Items[0].FatSat,
			FatTotal: 	fruitNutritionInfo.Items[0].FatTotal,
			Fiber: 		fruitNutritionInfo.Items[0].Fiber,
			Potassium: 	fruitNutritionInfo.Items[0].Potassium,
			Protein: 	fruitNutritionInfo.Items[0].Protein,
			Serving: 	fruitNutritionInfo.Items[0].Serving,
			Sodium: 	fruitNutritionInfo.Items[0].Sodium,
			Sugar: 		fruitNutritionInfo.Items[0].Sugar,
		}

		// Respond in JSON
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(200)

		json.NewEncoder(w).Encode(payload)
	}
}

// New Search Endpoint, takes in a request containing the users account details and item image.
// If the users' details are validated/confirmed then a new search for that item will begin
func SearchHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Println("File Upload Endpoint Hit")

	// Parse our multipart form, 10 << 20 specifies a maximum
	// upload of 10 MB files.
	//r.ParseMultipartForm(10 << 20)
	// FormFile returns the first file for the given key `myFile`
	// it also returns the FileHeader so we can get the Filename,
	// the Header and the size of the file
	file, handler, err := r.FormFile("myFile")
	if err != nil {
		fmt.Println("Error Retrieving the File")
		fmt.Println(err)
		return
	}
	defer file.Close()
	fmt.Printf("Uploaded File: %+v\n", handler.Filename)
	// fmt.Printf("File Size: %+v\n", handler.Size)
	// fmt.Printf("MIME Header: %+v\n", handler.Header)

	// Create a temporary file within our temp-images directory that follows
	// a particular naming pattern
	tempFile, err := ioutil.TempFile("temp-images", "upload-*.png")
	if err != nil {
		fmt.Println(err)
	}
	defer tempFile.Close()

	// read all of the contents of our uploaded file into a
	// byte array
	fileBytes, err := ioutil.ReadAll(file)
	if err != nil {
		fmt.Println(err)
	}
	// write this byte array to our temporary file
	tempFile.Write(fileBytes)
	// return that we have successfully uploaded our file!

	python := path.Clean(strings.Join([]string{"C:\\", "Users", "camak", "AppData", "Local", "Programs", "Python", "Python38", "python.exe"}, "\\"))
	script := path.Clean(strings.Join([]string{"C:\\", "Users", "camak", "Documents", "Projects", "University", "Team-12-Project-Test", "machine_learning", "predict.py"}, "\\"))
	item_file := "\\" + tempFile.Name()

	cmd := exec.Command(python, script, item_file)
	out, err := cmd.Output()
	if err != nil {
		log.Fatal(err)
	}

	predictionStr := string(out)
	// Response payload
	fruit := models.Fruit{
		Name: predictionStr,
	}
	// appsJson, errJson := json.Marshal(fruit)
	// if errJson != nil {
	// 	log.Fatal(errJson)
	// }

	// Respond in JSON
	//w.Header().Set("Content-Type", "application/json")
	//w.WriteHeader(200)

	json.NewEncoder(w).Encode(fruit)
	fmt.Println("Fruit Prediction Complete.")
}
