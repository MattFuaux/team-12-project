package models

// Declare structs here

// Fruit struct
type Fruit struct {
	Name string `json:"name"`
}

// User model
type User struct {
	ID        int    `json:"userID"`
	FirstName string `json:"firstName"`
	LastName  string `json:"lastName"`
	Email     string `json:"email"`
	Password  string `json:"password,omitempty"`
}

// Error model
type Error struct {
	Code    string `json:"code,omitempty"`
	Title   string `json:"title,omitempty"`
	Message string `json:"message,omitempty"`
}

type Errors struct {
	Errors []Error `json:"errors"`
}