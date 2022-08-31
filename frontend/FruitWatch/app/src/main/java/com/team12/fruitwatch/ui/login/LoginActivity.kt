package com.team12.fruitwatch.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.team12.fruitwatch.R
import com.team12.fruitwatch.data.model.LoggedInUser
import com.team12.fruitwatch.databinding.ActivityLoginBinding
import com.team12.fruitwatch.ui.main.MainActivity
import kotlinx.coroutines.*
import org.aviran.cookiebar2.CookieBar

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firstname: EditText
    private lateinit var surname: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var login: Button
    private lateinit var layoutTgl: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firstname = binding.firstname
        surname = binding.surname
        val email = binding.email
        val password = binding.password
        confirmPassword = binding.confirmPassword
        login = binding.login
        val loading = binding.loading
        layoutTgl = binding.layoutTgl

        layoutTgl.setOnCheckedChangeListener { buttonView, isChecked ->
            layoutToggle(isChecked)
        }
        layoutToggle(false)

        loginViewModel = ViewModelProvider(this,LoginViewModelFactory())[LoginViewModel::class.java]


        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both email / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.emailError != null) {
                email.error = getString(loginState.emailError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                setResult(Activity.RESULT_OK)
                //Complete and destroy login activity once successful
                finish()
            }
        })

        email.afterTextChanged {
            loginViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        CoroutineScope(Dispatchers.IO).async {
                            loginViewModel.login(
                                email.text.toString(),
                                password.text.toString()
                            )
                        }
                }
                false
            }

            firstname.apply {
                afterTextChanged {
                    if(layoutTgl.isChecked) {
                        loginViewModel.registrationDataChanged(firstname.text.toString(),surname.text.toString(),email.text.toString(),password.text.toString(),confirmPassword.text.toString())
                    }
                }
            }
            surname.apply {
                afterTextChanged {
                    if(layoutTgl.isChecked) {
                        loginViewModel.registrationDataChanged(firstname.text.toString(),surname.text.toString(),email.text.toString(),password.text.toString(),confirmPassword.text.toString())
                    }
                }
            }
            confirmPassword.apply {
                afterTextChanged {
                    if(layoutTgl.isChecked) {
                        loginViewModel.registrationDataChanged(firstname.text.toString(),surname.text.toString(),email.text.toString(),password.text.toString(),confirmPassword.text.toString())
                    }
                }
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE

                if(layoutTgl.isChecked) {
                    CoroutineScope(Dispatchers.Main).launch {
                        loginViewModel.register(firstname.text.toString(),surname.text.toString(),email.text.toString(),password.text.toString())
                    }
                }else{
                    CoroutineScope(Dispatchers.Main).launch {
                        loginViewModel.login(email.text.toString(), password.text.toString())
                    }
                }
                loading.visibility = View.INVISIBLE
            }
        }
        if (MainActivity.IN_DEVELOPMENT) {
            email.text = Editable.Factory.getInstance().newEditable("janetdoe@mailbox.com")
            password.text = Editable.Factory.getInstance().newEditable("testpassword")

            firstname.text = Editable.Factory.getInstance().newEditable("janet")
            surname.text = Editable.Factory.getInstance().newEditable("doe")
            confirmPassword.text = Editable.Factory.getInstance().newEditable("janetdoe")
        }
        checkForValidJWT()
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        MainActivity.showLoginGreeting = true
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_KEY", LoggedInUser(model.userId, model.displayName,model.jwt))
        startActivity(intent)
    }


    private fun showLoginFailed(@StringRes errorString: Int) {
        CookieBar.build(this)
            .setTitle(errorString)
            .setBackgroundColor(R.color.red_500)
            .setIcon(R.drawable.ic_baseline_clear_24)
            .setAnimationIn(android.R.anim.slide_in_left, android.R.anim.slide_in_left)
            .setAnimationOut(android.R.anim.slide_out_right, android.R.anim.slide_out_right)
            .setDuration(4000) 
            .show()
    }

    // Show/hides the login or registration input boxes depending on the UI switch position
    private fun layoutToggle(showRegistrationLayout: Boolean){
        if(showRegistrationLayout) {
            firstname.visibility = View.VISIBLE
            surname.visibility = View.VISIBLE
            confirmPassword.visibility = View.VISIBLE
            login.text = getText(R.string.action_register_and_sign_in)
        }else{
                firstname.visibility = View.INVISIBLE
                surname.visibility = View.INVISIBLE
                confirmPassword.visibility = View.INVISIBLE
            login.text = getText(R.string.action_sign_in)
        }
    }

    // Checks Shared Preferences for an encrypted JWT.
    // if found checks with the server if it is valid:
    // if it is valid auto logs user in,
    // otherwise logs user out and resumes normal login process
    private fun checkForValidJWT(){
        val SHARED_PREF_NAME :String  = "FWS"
        val TOKEN_PREF_NAME :String = "ult"

        val preferences: SharedPreferences = applicationContext.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE)
        val currentJWT = preferences.getString(TOKEN_PREF_NAME,"None")
        if(currentJWT != "None"){
            CookieBar.build(this)
                .setTitle("Auto Logging In")
                .setBackgroundColor(R.color.green_500)
                .setIcon(R.drawable.ic_baseline_login_24)
                .setAnimationIn(android.R.anim.slide_in_left, android.R.anim.slide_in_left)
                .setAnimationOut(android.R.anim.slide_out_right, android.R.anim.slide_out_right)
                .setDuration(2000)
                .show()
            Log.i(TAG,"Auto Login Attempted")
            // Some entry found, checking if it is valid
            CoroutineScope(Dispatchers.Main).launch {
                loginViewModel.isEntryValid(currentJWT!!)
            }
        }else{
            Log.i(TAG,"Auto Login Attempt Skipped")
        }

    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
