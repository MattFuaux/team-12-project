package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/subosito/gotenv"

	routes "backend/routes"
)

// load env variables on startup
func init() {
	err := gotenv.Load()
	if err != nil {
		log.Fatal(err)
	}
}

func main() {

	// create new gorilla mux router
	r := mux.NewRouter()

	// set the routes
	routes.SetRoutes(r)

	// start http server
	fmt.Println("FruitWatch server started on port 8080")
	log.Fatal(http.ListenAndServe(":8080", r))

}
