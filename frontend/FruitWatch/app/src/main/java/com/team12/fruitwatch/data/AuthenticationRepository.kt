package com.team12.fruitwatch.data

import com.team12.fruitwatch.data.model.LoggedInUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class AuthenticationRepository(val dataSource: AuthenticationDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    suspend fun login(email: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(email, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    suspend fun register(firstname:String, surname: String, email:String, password: String): Result<LoggedInUser> {
        // handle login
        val registrationResult = dataSource.register(firstname, surname, email, password)

        if (registrationResult is Result.Success) {
            return login(email, password)
        }
        return registrationResult
    }

    suspend fun logout(jwt:String): Boolean {
        // handle login
        val result = dataSource.logout(jwt)

        if (result) {
            setLoggedInUser(null)
            return true
        }

        return false
    }

    suspend fun checkIfValid(jwt: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.checkIfValid(jwt)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }
        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser?) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}