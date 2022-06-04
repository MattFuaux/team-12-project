package repo

import (
	"backend/database"
	"backend/models"
	"database/sql"
	"errors"
	"log"
)

// insert user into the database and return the last insert ID or an error
func InsertUser(user models.User) (int, error) {
	// sql query
	query := `
			INSERT INTO tblUsers(fldFirstName, fldLastName, fldEmail, fldPassword)
			VALUES(?, ?, ?, ?)
			`

	// prepare statement
	stmt, err := database.DB.Prepare(query)
	if err != nil {
		log.Fatalln(err)
	}
	defer stmt.Close()

	// execute prepared statement and return result object
	result, err := stmt.Exec(user.FirstName, user.LastName, user.Email, user.Password)
	if err != nil {
		log.Fatalln(err)
	}

	// check no. of rows affected
	rowsAffected, err := result.RowsAffected()
	if err != nil {
		log.Fatalln(err)
	}
	// if no rows are affected, return with -1 and the error
	if rowsAffected == 0 {
		return -1, errors.New("no rows affected")
	}

	// retrieve last insert ID from the insert
	lastInsertID, err := result.LastInsertId()
	if err != nil {
		log.Fatalln(err)
	}

	return int(lastInsertID), nil
}

// return user object by email address
func GetUserByID(id int) (models.User, error) {
	var user models.User
	// sql query
	query := `
			SELECT 
				fldID, 
				fldFirstName,
    			fldLastName,
    			fldEmail, 
    			fldPassword 
			FROM 
				tblUsers
			WHERE 
				fldID = ?;
			`
	// prepare statement
	stmt, err := database.DB.Prepare(query)
	if err != nil {
		log.Fatalln(err)
	}
	defer stmt.Close()

	// execute query row (as we are only expecting one row returned) and scan result to user
	err = stmt.QueryRow(id).Scan(&user.ID, &user.FirstName, &user.LastName, &user.Email, &user.Password)
	if err != nil {
		if err == sql.ErrNoRows { // no rows returned
			return models.User{}, errors.New("row not found")
		}
		log.Fatalln(err)
	}
	// a row was returned, return user object
	return user, nil
}

// return user object by email address
func GetUserByEmail(email string) (models.User, error) {
	var user models.User
	// sql query
	query := `
			SELECT 
				fldID, 
				fldFirstName,
    			fldLastName,
    			fldEmail, 
    			fldPassword 
			FROM 
				tblUsers
			WHERE 
				fldEmail = ?;
			`
	// prepare statement
	stmt, err := database.DB.Prepare(query)
	if err != nil {
		log.Fatalln(err)
	}
	defer stmt.Close()

	// execute query row (as we are only expecting one row returned) and scan result to user
	err = stmt.QueryRow(email).Scan(&user.ID, &user.FirstName, &user.LastName, &user.Email, &user.Password)
	if err != nil {
		if err == sql.ErrNoRows { // no rows returned
			return models.User{}, errors.New("row not found")
		}
		log.Fatalln(err)
	}
	// a row was returned, return user object
	return user, nil
}