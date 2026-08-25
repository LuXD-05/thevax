package com.luxd.thevax.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AlertDialog
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

class ProfileFragment : Fragment(R.layout.fragment_profile) {

	private var _binding: FragmentProfileBinding? = null
	private val binding get() = _binding!!

	private lateinit var dbHelper: DatabaseHelper
	private lateinit var repo: UserRepository
	private var currentUser: User? = null

	private var therapies = listOf<Therapy>()
	private var conditions = listOf<ClinicalCondition>()
	private var userConditions = mutableListOf<ClinicalCondition>()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		_binding = FragmentProfileBinding.bind(view)

		// Inits
		dbHelper = DatabaseHelper(requireContext())
		repo = UserRepository(requireContext(), dbHelper)
		
		// Load user from session (hybrid approach: cache or DB load)
		currentUser = SessionService.getInstance(requireContext()).getUser()

		if (currentUser == null) {
			logout()
			return
		}

		// Get therapies
		therapies = repo.getTherapies()

		// Get all conditions + user's conditions
		conditions = repo.getConditions()
		userConditions = repo.getConditionsForUser(currentUser!!.id).toMutableList() //TODO: ???

		// Setup dropdown with therapies & conditions
		val dropdownTherapies = arrayOf("Nessuna") + therapies.map { it.name }.toTypedArray()
		val dropdownConditions = conditions.map { it.conditionName }.toTypedArray()
		(binding.autoCompleteTherapy as MaterialAutoCompleteTextView).setSimpleItems(dropdownTherapies)
		(binding.autoCompleteTherapy as MaterialAutoCompleteTextView).setSimpleItems(dropdownConditions)

		// HANDLERS

		binding.btnSaveProfile.setOnClickListener { saveProfile() }
		binding.btnAddCondition.setOnClickListener { showAddConditionDialog() }
		binding.btnLogout.setOnClickListener { logout() }

		loadData()
	}

	private fun loadData() {
		val user = currentUser ?: return // TODO: ???

		// Setup user fields
		binding.etFirstName.setText(user.firstName)
		binding.etLastName.setText(user.lastName)
		binding.etAge.setText(user.age.toString())

		if (user.sex == "F") {
			binding.rbMale.isChecked = false
			binding.rbFemale.isChecked = true
		} else {
			binding.rbMale.isChecked = true
			binding.rbFemale.isChecked = false
		}

		val currentTherapyName = therapies.find { it.id == user.therapyId }?.name ?: "Nessuna"
		binding.autoCompleteTherapy.setText(currentTherapyName, false)

		// Setup user's conditions
		renderConditions()
	}

    //Update the list of conditions showing them as Chip
	private fun renderConditions() {

        //Cleans existing chips before recreating them
		binding.chipGroupConditions.removeAllViews()

		for (condition in userConditions) {
			val chip = Chip(requireContext()).apply {
				text = condition.conditionName
				isCloseIconVisible = true
				setOnCloseIconClickListener {
					userConditions.remove(condition)
				}
			}

            // Adds the Chip to the conditions group
			binding.chipGroupConditions.addView(chip)
		}

		// TODO: Selectable conditions
        // Show the message if there are no conditions, otherwise hide it
		binding.tvNoConditions.visibility = if (conditions.isEmpty()) View.VISIBLE else View.GONE
	}

	private fun saveProfile() {
		val user = currentUser ?: return

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
		if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank() || ageStr.isBlank()) {
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
		fields["conditions"] = userConditions.toList()

		// Updates the user in db
		if (repo.update(SessionService.getInstance(requireContext()).getUserId(), fields)) {
			Snackbar.make(binding.root, "Profilo aggiornato", Snackbar.LENGTH_LONG).show()
		} else {
			showError("Errore durante il salvataggio.")
		}
	}

	// TODO: impedire creazione di nuove conditions (solo dropdown da cui si selezionano conditions statiche dal db), controllare tutto ok, già modificato da lucix
	private fun showAddConditionDialog() {
		// Filter conditions not already added
		val selectableConditions = conditions.filter { it.id !in userConditions.map { uc -> uc.id }.toSet() }

		// TODO: far apparire ma non tramite snackbar, più pulito nel popup che si apre per l'aggiunta delle condizioni cliniche
		// (che poi tale textview dovrebbe esserci gia che mi pare di averla vista)
		if (selectableConditions.isEmpty()) {
			Snackbar.make(binding.root, "Nessuna nuova condizione disponibile", Snackbar.LENGTH_SHORT).show()
			return
		}

		// Shows conditions dialog
		AlertDialog.Builder(requireContext())
			.setTitle(getString(R.string.add_condition))
			.setItems(selectableConditions.map { it.conditionName }.toTypedArray()) { _, which ->
				val selected = selectableConditions[which]
				userConditions.add(selected)
			}
			.setNegativeButton(getString(R.string.cancel), null)
			.show()
	}

	private fun logout() {
		// Clears user in session
		SessionService.getInstance(requireContext()).clear()
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
