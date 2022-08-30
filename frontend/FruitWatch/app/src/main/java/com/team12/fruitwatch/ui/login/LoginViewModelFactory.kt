package com.team12.fruitwatch.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.team12.fruitwatch.data.AuthenticationDataSource

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                    authenticationDataSource = AuthenticationDataSource()
            )as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}