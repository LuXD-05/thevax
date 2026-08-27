package com.luxd.thevax.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.luxd.thevax.R
import com.luxd.thevax.databinding.ActivityRegisterBinding
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.DAOs.TherapyDAO
import com.luxd.thevax.db.entities.ClinicalCondition
import com.luxd.thevax.db.entities.RegisterDTO
import com.luxd.thevax.db.DAOs.ClinicalConditionDAO
import com.luxd.thevax.db.entities.Therapy
import com.luxd.thevax.db.repositories.UserRepository

class RegisterActivity : AppCompatActivity() {

	private lateinit var binding: ActivityRegisterBinding

	private val db by lazy { DatabaseHelper(this) }
	private val repo by lazy { UserRepository(db) }

	private val therapyDAO by lazy { TherapyDAO(db.writableDatabase) }
	private val conditionDAO by lazy { ClinicalConditionDAO(db.writableDatabase) }

	private var availableConditions = listOf<ClinicalCondition>()
	private val conditions = mutableListOf<ClinicalCondition>()
	private var therapies = listOf<Therapy>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		binding = ActivityRegisterBinding.inflate(layoutInflater)
		setContentView(binding.root)

		ViewCompat.setOnApplyWindowInsetsListener(binding.register) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}

		// Get available therapies
		therapies = therapyDAO.getAll()

		// Get available Conditions
		availableConditions = conditionDAO.getAll()

		// Setup dropdown with therapies
		val dropdownTherapies = arrayOf("Nessuna") + therapies.map { it.name }.toTypedArray()
		(binding.autoCompleteTherapy as MaterialAutoCompleteTextView).setSimpleItems(dropdownTherapies)

		// Setup dropdown with conditions
		val dropdownConditions = availableConditions.map { it.conditionName }.toTypedArray()
		(binding.autoCompleteConditions as MaterialAutoCompleteTextView).setSimpleItems(dropdownConditions)


		// Setup multi-select dropdown for conditions
		binding.autoCompleteConditions.setOnItemClickListener { _, _, position, _ ->
			val selected = availableConditions[position]
			if (conditions.none { it.id == selected.id }) {
				conditions.add(selected)
				renderConditions()
			}
			binding.autoCompleteConditions.setText("", false)
		}

		// HANDLERS

		// Back button
		binding.toolbar.setNavigationOnClickListener {
			onBackPressedDispatcher.onBackPressed()
		}
		// Register button
		binding.btnRegister.setOnClickListener {
			register()
		}
	}

	/**
	 * Gets register data from DTO + validates it + registers the user in DB
	 */
	private fun register() {
		// Get user data from DTO
		val email = binding.etEmail.text.toString().trim()
		val password = binding.etPassword.text.toString()
		val confirm = binding.etConfirmPassword.text.toString()
		val firstName = binding.etFirstName.text.toString().trim()
		val lastName = binding.etLastName.text.toString().trim()
		val ageStr = binding.etAge.text.toString()
		val sex = if (binding.radioFemale.isChecked) "F" else "M"

		// Get selected therapy
		val selectedTherapyText = binding.autoCompleteTherapy.text.toString()
		val selectedTherapyId = therapies.find { it.name == selectedTherapyText }?.id

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
		if (selectedTherapyText != "Nessuna" && selectedTherapyId == null) {
			showError("Terapia invalida.")
			return
		}

		val registerInfo = RegisterDTO(
			email = email,
			password = password,
			firstName = firstName,
			lastName = lastName,
			age = age,
			sex = sex,
			therapyId = selectedTherapyId,
			conditions = conditions.toList()
		)

		// Registers the user in db
		val user = repo.register(registerInfo)

		if (user != null) {
			startActivity(Intent(this, MainActivity::class.java))
			finish()
		} else {
			showError("Errore: questa email è già registrata o non è stato possibile salvare i dati.")
		}
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
		binding.chipGroupConditions.visibility = if (conditions.isEmpty()) View.GONE else View.VISIBLE
		binding.tvNoConditions.visibility = if (conditions.isEmpty()) View.VISIBLE else View.GONE
	}

	private fun showError(msg: String) {
		Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
	}
}
