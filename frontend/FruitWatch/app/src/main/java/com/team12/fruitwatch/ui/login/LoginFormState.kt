package com.team12.fruitwatch.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val firstnameError: Int? = null,
    val surnameError: Int? = null,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null,
    val isDataValid: Boolean = false
)