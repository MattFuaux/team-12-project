package com.team12.fruitwatch.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.team12.fruitwatch.R
import com.team12.fruitwatch.data.AuthenticationDataSource
import com.team12.fruitwatch.data.Result

// This view model keeps the pieces of data shown on the Login Activity in sync with the changes that they incur in the background from extrenal sources (server)
class LoginViewModel(private val authenticationDataSource: AuthenticationDataSource) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    // Sends the login information to the Authentication Data Source, then returns the result to the UI (Login Activity)
    suspend fun login(email: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = authenticationDataSource.login(email, password)

        if (result is Result.Success) {
            _loginResult.value = LoginResult(success = LoggedInUserView(result.data.userId,result.data.displayName,result.data.jwt))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    // Sends the login information to the Authentication Data Source, then returns the result to the UI (Login Activity)
    suspend fun register(firstname:String, surname:String, email: String, password: String) {
        // can be launched in a separate asynchronous job
        val registrationResult = authenticationDataSource.register(firstname, surname, email, password)

        if (registrationResult is Result.Success) {
            _loginResult.value = LoginResult(success = LoggedInUserView(registrationResult.data.userId,registrationResult.data.displayName,registrationResult.data.jwt))
        } else {
            if((registrationResult as Result.Error).exception.message == "Email already exists"){
                _loginResult.value = LoginResult(error = R.string.registration_email_taken)
            }else{
                _loginResult.value = LoginResult(error = R.string.registration_failed)
            }
        }
    }

    // Sends the logout/JWT information to the Authentication Data Source, then returns the result to the UI (Login Activity)
    suspend fun logout(jwt:String) : Boolean {
        return authenticationDataSource.logout(jwt)
    }

    // Sends the users JWT information to the Authentication Data Source, then returns the result to the UI (Login Activity)
    suspend fun isEntryValid(jwt: String)  {
        // can be launched in a separate asynchronous job
        val result = authenticationDataSource.checkIfValid(jwt)
        if (result is Result.Success) {
            _loginResult.value = LoginResult(success = LoggedInUserView(result.data.userId,result.data.displayName,result.data.jwt))
        } else {
            _loginResult.value = LoginResult(error = R.string.check_valid_failed)
        }
    }

    // Updates the login information that has been entered on the UI (Login Activity) in order to validate the input
    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // Updates the registration information that has been entered on the UI (Login Activity) in order to validate the input
    fun registrationDataChanged(firstname:String, surname:String, email: String, password: String, confirmedPassword:String) {
        if (!isNameValid(firstname)) {
            _loginForm.value = LoginFormState(firstnameError = com.team12.fruitwatch.R.string.invalid_firstname)
        } else if (!isNameValid(surname)) {
            _loginForm.value = LoginFormState(surnameError = com.team12.fruitwatch.R.string.invalid_surname)
        } else if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else if (!isPasswordValid(confirmedPassword)) {
            _loginForm.value = LoginFormState(confirmPasswordError = R.string.invalid_confirmed_password)
        }else if (!isConfirmedPasswordEqualToPassword(password,confirmedPassword)) {
            _loginForm.value = LoginFormState(confirmPasswordError = R.string.inequal_passwords)
        }else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // The first/surname validation check test is done here
    private fun isNameValid(name: String): Boolean {
        return name.isNotBlank()
    }

    // The email validation check test is done here
    private fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && (email.contains("@"))
    }

    // The password validation check test is done here
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    // The confirmed password equality validation check test is done here
    private fun isConfirmedPasswordEqualToPassword(password: String, confirmedPassword: String): Boolean {
        return confirmedPassword == password
    }
}