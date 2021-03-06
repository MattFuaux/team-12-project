package routes

import (
	controllers "backend/controllers"
	"backend/middlewares"

	"github.com/gorilla/mux"
)

// Define API routes here with the controller
func SetRoutes(r *mux.Router) {
	r.HandleFunc("/ping", controllers.PingHandler).Methods("GET")
	r.HandleFunc("/register", controllers.RegisterHandler).Methods("POST")
	r.HandleFunc("/authenticate", controllers.AuthHandler).Methods("POST")
	r.HandleFunc("/logout", controllers.LogoutHandler).Methods("POST")
	// Secured endpoint. Client is required to be authenticated to access.
	r.HandleFunc("/search", middlewares.ValidateJWT(controllers.SearchHandler)).Methods("POST")
}
