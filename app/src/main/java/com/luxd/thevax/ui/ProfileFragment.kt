package com.luxd.thevax.ui

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.luxd.thevax.R
import com.luxd.thevax.databinding.FragmentProfileBinding
import com.luxd.thevax.db.DAOs.UserDAO
import com.luxd.thevax.db.DAOs.TherapyDAO
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.ClinicalCondition
import com.luxd.thevax.db.entities.Therapy
import com.luxd.thevax.db.entities.User
import com.luxd.thevax.services.SessionService

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userDao: UserDAO
    private lateinit var therapyDao: TherapyDAO

    private val conditions = mutableListOf<ClinicalCondition>()
    private var therapiesList = listOf<Therapy>()
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        userDao = UserDAO(dbHelper.writableDatabase)
        therapyDao = TherapyDAO(dbHelper.readableDatabase)

        setupTherapyDropdown()
        loadUserData()

        binding.btnSaveProfile.setOnClickListener { saveProfile() }
        binding.btnAddCondition.setOnClickListener { showAddConditionDialog() }
        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun setupTherapyDropdown() {
        therapiesList = therapyDao.getAll()
        val labels = mutableListOf(getString(R.string.none))
        labels.addAll(therapiesList.map { it.name })

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, labels)
        binding.autoCompleteTherapy.setAdapter(adapter)
    }

    private fun loadUserData() {
        val session = SessionService.getInstance(requireContext())
        val userId = session.getUserId()

        if (userId == -1) {
            logout()
            return
        }

        currentUser = userDao.findById(userId)
        val user = currentUser ?: return

        binding.etFirstName.setText(user.firstName)
        binding.etLastName.setText(user.lastName)
        binding.etAge.setText(user.age.toString())

        if (user.sex == "F") {
            binding.rbFemale.isChecked = true
        } else {
            binding.rbMale.isChecked = true
        }

        user.therapyId?.let { tid ->
            val therapy = therapiesList.find { it.id == tid }
            therapy?.let {
                binding.autoCompleteTherapy.setText(it.name, false)
            }
        } ?: run {
            binding.autoCompleteTherapy.setText(getString(R.string.none), false)
        }

        loadConditions(user.id)
    }

    private fun loadConditions(userId: Int) {
        conditions.clear()
        val db = dbHelper.readableDatabase
        val sql = "SELECT c.id, c.name FROM conditions c " +
                "JOIN user_conditions uc ON c.id = uc.condition_id " +
                "WHERE uc.user_id = ?"

        try {
            db.rawQuery(sql, arrayOf(userId.toString())).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    conditions.add(ClinicalCondition(id, userId.toLong(), name))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        renderConditions()
    }

    private fun saveProfile() {
        val user = currentUser ?: return
        val ageStr = binding.etAge.text.toString()
        val age = ageStr.toIntOrNull()

        if (age == null || age !in 0..130) {
            showError("Inserisci un'età valida (0-130).")
            return
        }

        val sex = if (binding.rbFemale.isChecked) "F" else "M"
        
        val selectedText = binding.autoCompleteTherapy.text.toString()
        val selectedIndex = therapiesList.map { it.name }.indexOf(selectedText)
        val therapyId = if (selectedIndex != -1) therapiesList[selectedIndex].id else null

        val fields = mutableMapOf<String, Any?>()
        fields["age"] = age
        fields["sex"] = sex
        fields["therapy_id"] = therapyId
        fields["first_name"] = binding.etFirstName.text.toString().trim()
        fields["last_name"] = binding.etLastName.text.toString().trim()

        if (userDao.update(user.id, fields)) {
            currentUser = user.copy(
                firstName = fields["first_name"] as String,
                lastName = fields["last_name"] as String,
                age = age,
                sex = sex,
                therapyId = therapyId
            )
            Snackbar.make(binding.root, "Profilo aggiornato!", Snackbar.LENGTH_SHORT).show()
        } else {
            showError("Errore durante il salvataggio.")
        }
    }

    private fun showAddConditionDialog() {
        val etName = EditText(requireContext())
        etName.hint = getString(R.string.condition_name)

        val container = LinearLayout(requireContext())
        container.setPadding(50, 40, 50, 10)
        container.addView(
            etName,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_condition))
            .setView(container)
            .setPositiveButton(getString(R.string.add), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val name = etName.text.toString().trim()
                    if (name.isNotEmpty()) {
                        val userId = currentUser?.id ?: return
                        val db = dbHelper.writableDatabase

                        var condId = -1L
                        db.rawQuery("SELECT id FROM conditions WHERE name = ?", arrayOf(name)).use { cursor ->
                            if (cursor.moveToFirst()) condId = cursor.getLong(0)
                        }

                        if (condId == -1L) {
                            val values = ContentValues()
                            values.put("name", name)
                            condId = db.insert("conditions", null, values)
                        }

                        if (condId != -1L) {
                            val values = ContentValues()
                            values.put("user_id", userId)
                            values.put("condition_id", condId)
                            db.insert("user_conditions", null, values)

                            conditions.add(ClinicalCondition(condId, userId.toLong(), name))
                            renderConditions()
                        }
                    }
                }
            })
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun renderConditions() {
        binding.chipGroupConditions.removeAllViews()

        for (condition in conditions) {
            val chip = Chip(requireContext())
            chip.text = condition.conditionName
            chip.isCloseIconVisible = true

            chip.setOnCloseIconClickListener {
                dbHelper.writableDatabase.delete(
                    "user_conditions",
                    "user_id = ? AND condition_id = ?",
                    arrayOf(currentUser?.id.toString(), condition.id.toString())
                )

                conditions.remove(condition)
                renderConditions()
            }

            binding.chipGroupConditions.addView(chip)
        }

        binding.tvNoConditions.visibility = if (conditions.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun logout() {
        SessionService.getInstance(requireContext()).clear()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
