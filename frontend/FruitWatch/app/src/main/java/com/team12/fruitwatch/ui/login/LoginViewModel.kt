package com.team12.fruitwatch.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.team12.fruitwatch.R
import com.team12.fruitwatch.data.AuthenticationRepository
import com.team12.fruitwatch.data.Result

class LoginViewModel(private val loginRepository: AuthenticationRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    suspend fun login(email: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(email, password)

        if (result is Result.Success) {
            _loginResult.value = LoginResult(success = LoggedInUserView(result.data.userId,result.data.displayName,result.data.jwt))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    suspend fun register(firstname:String, surname:String, email: String, password: String) {
        // can be launched in a separate asynchronous job
        val registrationResult = loginRepository.register(firstname, surname, email, password)

        if (registrationResult is Result.Success) {
            _loginResult.value = LoginResult(success = LoggedInUserView(registrationResult.data.userId,registrationResult.data.displayName,registrationResult.data.jwt))
        } else {
            _loginResult.value = LoginResult(error = R.string.registration_failed)
        }
    }

    suspend fun logout(jwt:String) : Boolean {
        return loginRepository.logout(jwt)
    }

    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun registrationDataChanged(firstname:String, surname:String, email: String, password: String, confirmedPassword:String) {
        if (!isNameValid(firstname)) {
            _loginForm.value = LoginFormState(firstnameError = com.team12.fruitwatch.R.string.invalid_firstname)
        } else if (!isNameValid(surname)) {
            _loginForm.value = LoginFormState(surnameError = com.team12.fruitwatch.R.string.invalid_surname)
        } else if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else if (!isConfirmedPasswordValid(password, confirmedPassword)) {
            _loginForm.value = LoginFormState(confirmPasswordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder email validation check
    private fun isNameValid(name: String): Boolean {
        return name.isNotBlank()
    }

    // A placeholder email validation check
    private fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && (email.contains("@"))
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    // A placeholder password validation check
    private fun isConfirmedPasswordValid(password: String, confirmedPassword: String): Boolean {
        return password.length > 5 && (confirmedPassword == password)
    }
}