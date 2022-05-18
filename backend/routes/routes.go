package routes

import (
	controllers "backend/controllers"

	"github.com/gorilla/mux"
)

// Define API routes here with the controller
func SetRoutes(r *mux.Router) {
	r.HandleFunc("/ping", controllers.PingHandler).Methods("GET")
	r.HandleFunc("/nutrition", controllers.NutritionHandler).Methods("POST")
	r.HandleFunc("/upload", controllers.SearchHandler)
}
