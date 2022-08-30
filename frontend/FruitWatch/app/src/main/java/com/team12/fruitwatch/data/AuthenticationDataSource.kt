package com.team12.fruitwatch.data

import com.team12.fruitwatch.controllers.NetworkRequestController
import com.team12.fruitwatch.data.model.LoggedInUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class AuthenticationDataSource {

    // Send the user login information to the network controller and returns the appropriate response depending on the network controller result
    suspend fun login(email: String, password: String): Result<LoggedInUser> {
        return withContext(Dispatchers.IO) {
            try {
                val networkRequestController = NetworkRequestController()
                val response = networkRequestController.loginUser(email, password)
                if (response.statusCode == 200) {
                    return@withContext Result.Success(response.data as LoggedInUser)
                }
                throw Exception()

            } catch (e: Exception) {
                return@withContext Result.Error(IOException("Error logging in", e))
            }
        }
    }

    // Send the user registration information to the network controller then immediately logs the user in then returns the appropriate response depending on the network controller result
    suspend fun register(firstname: String, surname: String, email: String, password: String): Result<LoggedInUser> {
        return withContext(Dispatchers.IO) {
            try {
                val networkRequestController = NetworkRequestController()
                val registrationResponse = networkRequestController.registerUser(firstname, surname, email, password)
                if (registrationResponse.statusCode == 200) {
                    val loginResponse = networkRequestController.loginUser(email, password)
                    if (loginResponse.statusCode == 200) {
                        return@withContext Result.Success(loginResponse.data as LoggedInUser)
                    }
                    throw Exception("Error Logging User In")
                }
                throw Exception("Error Registering User")
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }
    }

    // Send the user logout information to the network controller and returns the appropriate response depending on the network controller result
    suspend fun logout(jwt: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val networkRequestController = NetworkRequestController()
                val response = networkRequestController.logoutUser(jwt)
                // if the response data is null then the server received and invalidated the JWT so the user will be logged out
                if (response.statusCode == 200 && response.data == null) {
                    return@withContext true
                }
                throw Exception("Error Logging User Out")
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }

    // Send the users JWT information to the network controller to check if the JWT has expired and then returns the appropriate response depending on the network controller result
    suspend fun checkIfValid(jwt: String): Result<LoggedInUser> {
        return withContext(Dispatchers.IO) {
            try {
                val networkRequestController = NetworkRequestController()
                val checkResponse = networkRequestController.checkIfValid(jwt)
                if (checkResponse.statusCode == 200) {
                    return@withContext Result.Success(checkResponse.data as LoggedInUser)
                }
                throw Exception("Error Logging User In")
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }
    }
}