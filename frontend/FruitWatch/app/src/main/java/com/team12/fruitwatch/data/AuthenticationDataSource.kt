package com.team12.fruitwatch.data

import com.team12.fruitwatch.controllers.NetworkRequestController
import com.team12.fruitwatch.data.model.LoggedInUser
import kotlinx.coroutines.*
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class AuthenticationDataSource {

    suspend fun login(email: String, password: String): Result<LoggedInUser> {
        return withContext(Dispatchers.IO){
            try {
                // TODO: handle loggedInUser authentication
                val networkRequestController = NetworkRequestController()
                val response = networkRequestController.loginUser(email, password)
                if(response.statusCode == 200){
                    return@withContext Result.Success(response.data as LoggedInUser)
                }
                throw Exception()
                //val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe","jwt")
            } catch (e: Exception) {
                return@withContext Result.Error(IOException("Error logging in", e))
            }
        }
    }

    suspend fun register(firstname:String, surname: String, email:String, password: String): Result<LoggedInUser> {
        return withContext(Dispatchers.IO){
            try {
                val networkRequestController = NetworkRequestController()
                val registrationResponse = networkRequestController.registerUser(firstname, surname, email, password)
                if(registrationResponse.statusCode == 200){
                    val loginResponse = networkRequestController.loginUser(email, password)
                    if(loginResponse.statusCode == 200){
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

    suspend fun logout(jwt: String) : Boolean {
        return withContext(Dispatchers.IO){
            try {
                val networkRequestController = NetworkRequestController()
                val response = networkRequestController.logoutUser(jwt)
                if(response.statusCode == 200){
                    return@withContext true
                }
                throw Exception("Error Logging User Out")
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }
}