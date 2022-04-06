package controllers

import (
	"backend/models"
	"encoding/json"
	"net/http"
	"os"
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
