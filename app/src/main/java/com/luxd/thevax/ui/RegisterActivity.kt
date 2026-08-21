package com.luxd.thevax.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.luxd.thevax.R
import com.luxd.thevax.databinding.ActivityRegisterBinding
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.DAOs.TherapyDAO
import com.luxd.thevax.db.entities.ClinicalCondition
import com.luxd.thevax.db.entities.RegisterDTO
import com.luxd.thevax.db.entities.Therapy
import com.luxd.thevax.db.repositories.UserRepository

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val conditions = mutableListOf<ClinicalCondition>()
    private var therapiesList = listOf<Therapy>()

    private lateinit var repo: UserRepository
    private lateinit var therapyDAO: TherapyDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbHelper = DatabaseHelper(this)
        repo = UserRepository(this, dbHelper)
        therapyDAO = TherapyDAO(dbHelper.readableDatabase)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.register) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnRegister.setOnClickListener { register() }

        setupTherapyDropdown()
        binding.btnAddCondition.setOnClickListener { showAddConditionDialog() }
    }

    private fun setupTherapyDropdown() {
        therapiesList = therapyDAO.getAll()
        val labels = mutableListOf(getString(R.string.none))
        labels.addAll(therapiesList.map { it.name })

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, labels)
        binding.autoCompleteTherapy.setAdapter(adapter)
    }

    private fun register() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val ageStr = binding.etAge.text.toString()
        val sex = if (binding.radioFemale.isChecked) "F" else "M"

        val selectedText = binding.autoCompleteTherapy.text.toString()
        val selectedIndex = therapiesList.map { it.name }.indexOf(selectedText)
        val therapyId = if (selectedIndex != -1) therapiesList[selectedIndex].id else null

        if (email.isBlank() || password.isBlank() || confirm.isBlank() || firstName.isBlank() || lastName.isBlank() || ageStr.isBlank()) {
            showError("Tutti i campi sono obbligatori.")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Inserisci un'email valida.")
            return
        }
        if (password.length < 8) {
            showError("La password deve avere almeno 8 caratteri.")
            return
        }
        if (password != confirm) {
            showError("Le password non coincidono.")
            return
        }
        val age = ageStr.toIntOrNull()
        if (age == null || age < 0 || age > 130) {
            showError("Inserisci un'età valida (0-130).")
            return
        }

        val registerInfo = RegisterDTO(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            age = age,
            sex = sex,
            therapyId = therapyId,
            conditions = conditions.toList()
        )
        val user = repo.register(registerInfo)

        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            showError("Errore: questa email è già registrata o non è stato possibile salvare i dati.")
        }
    }

    private fun showAddConditionDialog() {
        val conditionName = EditText(this).apply {
            hint = getString(R.string.condition_name)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.add_condition)
            .setView(conditionName)
            .setPositiveButton(R.string.add, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = conditionName.text.toString().trim()
                if (name.isBlank()) {
                    conditionName.error = getString(R.string.condition_name)
                    return@setOnClickListener
                }
                if (conditions.any { it.conditionName.equals(name, ignoreCase = true) }) {
                    conditionName.error = getString(R.string.condition_already_added)
                    return@setOnClickListener
                }
                conditions.add(ClinicalCondition(userId = 0, conditionName = name))
                renderConditions()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun renderConditions() {
        binding.chipGroupConditions.removeAllViews()
        conditions.forEach { condition ->
            val chip = Chip(this).apply {
                text = condition.conditionName
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    conditions.remove(condition)
                    renderConditions()
                }
            }
            binding.chipGroupConditions.addView(chip)
        }
        binding.tvNoConditions.visibility = if (conditions.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }
}
