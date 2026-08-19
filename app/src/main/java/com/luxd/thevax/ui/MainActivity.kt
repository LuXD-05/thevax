package com.luxd.thevax.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.luxd.thevax.services.SessionService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        // If not logged in, go directly to login
        if (SessionService.getInstance(this).isLoggedIn()) {
            startActivity(Intent(this, SplashScreen::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}