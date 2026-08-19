package com.luxd.thevax.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.luxd.thevax.databinding.ActivityLoginBinding
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.repositories.UserRepository
import com.luxd.thevax.services.SessionService

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var repo: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repo = UserRepository(this, DatabaseHelper(this))

        // If already logged, go directly to MainActivity
        if (SessionService.getInstance(this).isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.login) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handler click login button
        binding.btnLogin.setOnClickListener {
            login()
        }

        // Handler click "Register" text
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // 1. Check empty fields
        if (email.isBlank() || password.isBlank()) {
            showError("Compila tutti i campi.")
            return
        }

        // 2. Check email with regex
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Inserisci un'email valida.")
            return
        }

        // If validation went fine, then login
        val user = repo.login(email, password)

        // If user logged
        if (user != null) {
            // Load MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            showError("Errore: credenziali errate")
        }
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

}
