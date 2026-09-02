package com.luxd.thevax.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.luxd.thevax.R
import com.luxd.thevax.databinding.FragmentProfileBinding
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.ClinicalCondition
import com.luxd.thevax.db.entities.Therapy
import com.luxd.thevax.db.entities.User
import com.luxd.thevax.db.repositories.UserRepository
import com.luxd.thevax.services.SessionService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

	private var _binding: FragmentProfileBinding? = null
	private val binding get() = _binding!!

	private val db by lazy { DatabaseHelper(requireContext()) }
	private val repo by lazy { UserRepository(db) }

	private var availableConditions = listOf<ClinicalCondition>()
	private var filteredConditions = listOf<ClinicalCondition>()
	private val conditions = mutableListOf<ClinicalCondition>()
	private var therapies = listOf<Therapy>()

	private lateinit var currentUser: User

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		_binding = FragmentProfileBinding.bind(view)

		// Handlers
		binding.btnSaveProfile.setOnClickListener { saveProfile() }
		binding.btnLogout.setOnClickListener { logout() }

		loadData()
	}

	private fun loadData() {
		viewLifecycleOwner.lifecycleScope.launch {

			// Load user from session (cache or DB)
			currentUser = SessionService.getInstance().getUser() ?: return@launch logout()

			// Get therapies + conditions
			therapies = repo.getTherapies()
			availableConditions = repo.getConditions()

			// Load existing user conditions
			conditions.clear()
			conditions.addAll(repo.getConditionsForUser(currentUser.id))

			// Setup dropdown with therapies
			val dropdownTherapies = arrayOf("Nessuna") + therapies.map { it.name }.toTypedArray()
			(binding.autoCompleteTherapy as MaterialAutoCompleteTextView).apply {
				setSimpleItems(dropdownTherapies)

				// Disable filtering so all items are shown even after selection or rotation
				threshold = Int.MAX_VALUE

				// Clear filter that might be applied by restored text after rotation
				setText(text.toString(), false)

				setOnItemClickListener { _, _, position, _ ->
					val selected = dropdownTherapies[position]
					setText(selected, false)
				}
			}

			updateConditionsDropdown()

			// Setup multi-select dropdown for conditions
			binding.autoCompleteConditions.setOnItemClickListener { _, _, position, _ ->
				val selected = filteredConditions[position]
				if (conditions.none { it.id == selected.id }) {
					conditions.add(selected)
					renderConditions()
				}
				binding.autoCompleteConditions.setText("", false)
			}

			// Setup user fields
			binding.etFirstName.setText(currentUser.firstName)
			binding.etLastName.setText(currentUser.lastName)
			binding.etAge.setText(currentUser.age.toString())
			binding.etEmail.setText(currentUser.email)

			if (currentUser.sex == "F") {
				binding.rbMale.isChecked = false
				binding.rbFemale.isChecked = true
			} else {
				binding.rbMale.isChecked = true
				binding.rbFemale.isChecked = false
			}

			val currentTherapyName = therapies.find { it.id == currentUser.therapyId }?.name ?: "Nessuna"
			binding.autoCompleteTherapy.setText(currentTherapyName, false)

			// Setup user's conditions
			renderConditions()

		}
	}

	/**
	 * Renders the conditions chips
	 */
	private fun renderConditions() {
		binding.chipGroupConditions.removeAllViews()
		conditions.forEach { condition ->
			val chip = Chip(requireContext()).apply {
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

		updateConditionsDropdown()
	}

	/**
	 * Updates the available conditions dropdown
	 */
	private fun updateConditionsDropdown() {
		filteredConditions = availableConditions.filter { available ->
			conditions.none { it.id == available.id }
		}
		val items = filteredConditions.map { it.conditionName }.toTypedArray()
		(binding.autoCompleteConditions as MaterialAutoCompleteTextView).setSimpleItems(items)
	}

	/**
	 * Saves the user profile in db
	 */
	private fun saveProfile() {
		val firstName = binding.etFirstName.text.toString().trim()
		val lastName = binding.etLastName.text.toString().trim()
		val ageStr = binding.etAge.text.toString()
		val sex = if (binding.rbFemale.isChecked) "F" else "M"
		val email = binding.etEmail.text.toString().trim()
		val password = binding.etPassword.text.toString()

		// Get selected therapy
		val selectedTherapyText = binding.autoCompleteTherapy.text.toString()
		val selectedTherapyId = therapies.find { it.name == selectedTherapyText }?.id

		// Validates fields
		if (email.isBlank() || firstName.isBlank() || lastName.isBlank() || ageStr.isBlank()) {
			showError("Tutti i campi sono obbligatori.")
			return
		}
		if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			showError("Inserisci un'email valida.")
			return
		}
		if (!password.isBlank() && password.length < 8) {
			showError("La password deve avere almeno 8 caratteri.")
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

		// Builds dynamic map of users fields
		val fields = mutableMapOf<String, Any?>()
		fields["first_name"] = firstName
		fields["last_name"] = lastName
		fields["age"] = age
		fields["sex"] = sex
		fields["email"] = email
		fields["password"] = password
		fields["therapy_id"] = selectedTherapyId
		fields["conditions"] = conditions.toList()

		viewLifecycleOwner.lifecycleScope.launch {
			// Updates the user in db
			if (repo.update(currentUser.id, fields)) {
				Snackbar.make(binding.root, "Profilo aggiornato", Snackbar.LENGTH_LONG).show()
			} else {
				showError("Errore durante il salvataggio.")
			}
		}
	}

	private fun logout() {
		// Clears user in session
		SessionService.getInstance().clear()
		// Goes to login + clear backStack
		val intent = Intent(requireContext(), LoginActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		startActivity(intent)
	}

    //Show error if something goes wrong during the saveProfile
	private fun showError(msg: String) {
		Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
