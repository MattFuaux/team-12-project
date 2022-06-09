package middlewares

import (
	"backend/util"
	"context"
	"encoding/json"
	"net/http"
	"os"
	"strconv"

	"github.com/dgrijalva/jwt-go"
)

// token validation middleware for protected routes
func ValidateJWT(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		// retrieve jwt cookie from cookie if any
		cookie, err := r.Cookie("jwt")
		if err != nil {
			if err == http.ErrNoCookie {
				w.Header().Set("Content-Type", "application/json")
				w.WriteHeader(401)
				json.NewEncoder(w).Encode(util.NewError("", "Unauthorized user", "Valid authorization cookie not found"))

				return
			}
		}

		// verify token
		token, err := jwt.ParseWithClaims(cookie.Value, &jwt.StandardClaims{}, func(token *jwt.Token) (interface{}, error) {
			return []byte(os.Getenv("JWT_SECRET")), nil
		})

		// invalid token
		if err != nil {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(401)

			json.NewEncoder(w).Encode(util.NewError("", "Unauthorized user", "Invalid token"))
			return
		}

		// token verified
		claims := token.Claims.(*jwt.StandardClaims)
		userID, _ := strconv.Atoi(claims.Issuer)

		// this is how the userID value is accessed through the next function
		// create a context and bind the userID to the key with the same name
		// userID, _ := r.Context().Value("userID").(int)
		ctx := context.WithValue(r.Context(), "userID", userID)

		// call next function
		next.ServeHTTP(w, r.WithContext(ctx))
	}
}