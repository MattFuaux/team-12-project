package database

import (
	"database/sql"
	"fmt"
	"log"
	"os"

	_ "github.com/go-sql-driver/mysql"
)

var DB *sql.DB

// connects to the database and returns a DB instance
func ConnectDB() {
	dsn := buildDSN()

	db, err := sql.Open("mysql", dsn)
	if err != nil {
		log.Fatalln("something went wrong while opening database connection")
	}

	DB = db
}

// retrieves database info fron .env file and builds the data source name required to connect to MySQL DB
func buildDSN() string {
	return fmt.Sprintf("%v:%v@tcp(%v:%v)/%v", os.Getenv("DB_USER"), os.Getenv("DB_PASSWORD"), os.Getenv("DB_HOST"), os.Getenv("DB_PORT"), os.Getenv("DB_NAME"))
}