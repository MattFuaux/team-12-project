# FruitWatch Backend API

FruitWatch backend API

## Installation

### Prerequisite

Golang version 1.18 or greater.

### Clone Repo

```sh
git@github.com:MattFuaux/team-12-project.git
```

### Initialize Database

- Install MySQL Server and Workbench
- Start MySQL server and update connection details in the `.env ` file if required
- Load database schema located in `backend/static/schema/fruitwatchdb.sql`

### Start Go API Server

Change directory to the go backend folder

```sh
cd team-12-project/backend
```

Download the Go API dependancies (list of dependancies are located in `backend/go.mod`)

```sh
go get
```

Start the Go server

```sh
go run .
```

The Go server will start on localhost:8080 by default

## API Endpoints

### /register

Description: Register a user in the system.

Example Request:

```
{
    "firstName" : "John",
    "lastName" : "Doe",
    "email" : "john.doe@gmail.com",
    "password" : "Password1"
}
```

Example Response:

```
{
    "message": "registration successful",
    "status": "OK"
}
```

### /authenticate

Description: Authenticate a pre-existing user.

Example Request:

```
{
    "email" : "john.doe@gmail.com",
    "password" : "Password1"
}
```

Example Response:

```
{
    "userID": 3,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@gmail.com"
}
```

### /logout

Description: Logout an authenticated user.

Example Request:
N/A - No request payload is required.

Example Response:

```
{
    "msg:": "Logout Successful"
}
```

### /search (Protected Endpoint)

Description: Upload an image of a fruit to identify and retrieve nutritional information.

Note: Client must be authenticated (have a valid JWT token to access this endpoint)

Example Request:
Upload picture of a corn:

![corn](screenshots/0030.jpg)

Example Response:

```
{
    "name": "Corn",
    "calories": 93.9,
    "carbohydrates_total_g": 21,
    "cholesterol_mg": 0,
    "fat_saturated_g": 0.2,
    "fat_total_g": 1.5,
    "fiber_g": 2.4,
    "potassium_mg": 75,
    "protein_g": 3.4,
    "serving_size_g": 100,
    "sodium_mg": 1,
    "sugar_g": 4.6
}
```
