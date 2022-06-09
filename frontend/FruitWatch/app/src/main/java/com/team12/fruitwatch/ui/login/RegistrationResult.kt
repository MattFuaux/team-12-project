package com.team12.fruitwatch.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class RegistrationResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)