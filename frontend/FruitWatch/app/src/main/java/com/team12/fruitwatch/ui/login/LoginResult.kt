package com.team12.fruitwatch.ui.login

/**
 * Login result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)