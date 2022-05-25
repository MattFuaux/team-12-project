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
- Start MySQL server and update connection details in `.env ` file if required
- Load database schema located in `backend/static/schema/fruitwatchdb.sql`

### Start Go API Server

Change directory to the go backend folder

```sh
cd team-12-project/backend
```

Download the Go API dependancies (list of dependancies are located in the go.mod file)

```sh
go get
```

Start the Go server

```sh
go run .
```

The Go server will start on localhost:8080 by default

## Testing in Postman

Make a post request to localhost:8080/nutrition, navigate to the Body tab (change to raw) and enter in the following JSON:

```
{
    "name" : "papaya"
}
```

The fruit name can be substituted to any fruit. This will make a call to [CalorieNinja](https://calorieninjas.com/) API, retrieve the nutritional info and serve the result back.

Example:
![](screenshots/postman-example.png)

## Registration and Authentication

### Registration

Endpoint: ` localhost:8080/register`

Example JSON payload:

```
{
    "firstName" : "Marck",
    "lastName" : "Munoz",
    "email" : "marck527@gmail.com",
    "password" : "Password1"
}
```

### Authentication

Endpoint: `localhost:8080/authenticate`

Note: Upon successful authentication, the server will send back a valid JWT token which can be used for authentication.

Example JSON payload:

```
{
    "email" : "Marck527@gmail.com",
    "password" : "Password1"
}
```
