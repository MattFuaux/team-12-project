package controllers

import (
	"backend/models"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"os/exec"
	"path"
	"strings"
)

// Define different controllers here for the routes

// Simply replies with "Pong"
func PingHandler(w http.ResponseWriter, r *http.Request) {

	// reply in JSON
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(200)

	json.NewEncoder(w).Encode("Pong")
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
