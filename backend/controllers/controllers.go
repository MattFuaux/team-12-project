package controllers

import (
	"backend/models"
	"backend/repo"
	"backend/util"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"strconv"
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
		w.WriteHeader(200)

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
		w.WriteHeader(200)

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

// Nutrition endpoint. Accepts a fruit name and makes a call to CalorieNinjas API to retrieve and serve food nutrition info
func NutritionHandler(w http.ResponseWriter, r *http.Request) {

	// Declare a new Fruit struct to hold response from client
	var fruit models.Fruit

	// Try to decode the request body into the Fruit struct. If there is an error,
	// respond to the client with the error message and a 400 status code.
	err := json.NewDecoder(r.Body).Decode(&fruit)
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	// API endpoint of CalorieNinja + fruit name parameter passed by the client
	endpoint := "https://api.calorieninjas.com/v1/nutrition?query=" + fruit.Name

	// Make API call to CalorieNinja API
	client := &http.Client{}
	req, _ := http.NewRequest("GET", endpoint, nil)
	// Pass API key in header. The API key is an environment variable set in the .env file
	req.Header.Set("X-Api-Key", os.Getenv("CALORIE_NINJA_API_KEY"))
	resp, _ := client.Do(req)

	defer resp.Body.Close()

	// Response payload
	var payload map[string]interface{}

	// Decode body response from the API call and decode into payload
	json.NewDecoder(resp.Body).Decode(&payload)

	// Respond in JSON
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(200)

	json.NewEncoder(w).Encode(payload)
}
