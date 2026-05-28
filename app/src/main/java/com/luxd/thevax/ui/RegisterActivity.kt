package com.luxd.thevax.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.luxd.thevax.databinding.ActivityRegisterBinding
import com.luxd.thevax.MainActivity
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.ClinicalCondition
import com.luxd.thevax.db.entities.RegisterDTO
import com.luxd.thevax.db.entities.Therapy
import com.luxd.thevax.db.repositories.UserRepository

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val repo = UserRepository(this, DatabaseHelper(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRegister.setOnClickListener {
            register()
        }
    }

    private fun register() {
        // 1. Retrieve data with BINDING
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val ageStr = binding.etAge.text.toString()
        val sex = if (binding.radioFemale.isChecked) "F" else "M"   // no need to check
        // TODO: therapies e CC

        // 1. No fields can be empty
        if (email.isBlank() || password.isBlank() || confirm.isBlank() || firstName.isBlank() || lastName.isBlank() || ageStr.isBlank() || sex.isBlank()) {
            showError("Tutti i campi sono obbligatori.")
            return
        }
        // 2. Valid email with regex
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Inserisci un'email valida.")
            return
        }
        // 3. Check psw (minimum 8 char)
        if (password.length < 8) {
            showError("La password deve avere almeno 8 caratteri.")
            return
        }
        if (password != confirm) {
            showError("Le password non coincidono.")
            return
        }
        // 4. Check valid age (0-130)
        val age = ageStr.toIntOrNull()
        if (age == null || age < 0 || age > 130) {
            showError("Inserisci un'età valida (0-130).")
            return
        }

        // 3. DB LOGIC (Use of repository)
        val registerInfo = RegisterDTO(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            age = age,
            sex = sex,
            // TODO: cosa fare con questi??? c'è da rifare la struttura db prima di push
            therapies = emptyList<Therapy>(),
            conditions = emptyList<ClinicalCondition>()
        )

        // TODO: register deve ritornare un user (come login) null se errore
        val user = repo.register(registerInfo)

        // If user logged
        if (user != null) {
            // Load MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // The same email is already present
            showError("Errore: questa email è già registrata")
        }
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

}
