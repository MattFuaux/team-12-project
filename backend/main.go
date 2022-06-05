package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/subosito/gotenv"

	"backend/database"
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

	// connect to database and test connection
	database.ConnectDB()
	err := database.DB.Ping()
	if err != nil {
		log.Fatalln("error: unable to connect to the database.")
	} else {
		fmt.Print("Connected to the database.\n")
	}

	// create new gorilla mux router
	r := mux.NewRouter()

	// set the routes
	routes.SetRoutes(r)

	// start http server
	fmt.Println("FruitWatch server started on port 8080.")
	log.Fatal(http.ListenAndServe(":8080", r))

}
