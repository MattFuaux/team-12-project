package com.team12.fruitwatch.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.team12.fruitwatch.R
import com.team12.fruitwatch.ui.login.LoginActivity
import com.team12.fruitwatch.ui.main.MainActivity.Companion.IN_DEVELOPMENT


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (IN_DEVELOPMENT) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            val logo = findViewById<LinearLayout>(R.id.activity_splash_fruit_watch_logo)
            Handler().postDelayed({
                logo.startAnimation(animation)
            }, 1000)
            Handler().postDelayed({
                logo.visibility = View.VISIBLE
            }, 3000)
            Handler().postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 4000)
        }
    }
}