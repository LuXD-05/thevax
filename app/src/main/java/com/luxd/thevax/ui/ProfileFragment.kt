package com.luxd.thevax.ui

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.luxd.thevax.databinding.FragmentProfileBinding
import com.luxd.thevax.db.DAOs.UserDAO
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.ClinicalCondition
import com.luxd.thevax.db.entities.Therapy
import com.luxd.thevax.db.entities.User
import com.luxd.thevax.services.SessionService

class ProfileFragment : Fragment() {

    // Binding della view del fragment
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userDao: UserDAO

    private val therapies = mutableListOf<Therapy>()
    private val conditions = mutableListOf<ClinicalCondition>()

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

        loadUserData()

        binding.btnSaveProfile.setOnClickListener { saveProfile() }
        binding.btnAddTherapy.setOnClickListener { showAddTherapyDialog() }
        binding.btnAddCondition.setOnClickListener { showAddConditionDialog() }
        binding.btnLogout.setOnClickListener { logout() }
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

        loadTherapies(user.id)
        loadConditions(user.id)
    }

    private fun loadTherapies(userId: Int) {
        therapies.clear()
        val db = dbHelper.readableDatabase

        try {
            // Le terapie vengono lette dalla tabella e poi mostrate nella schermata.
            db.rawQuery("SELECT * FROM therapies", null).use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    val description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: ""

                    therapies.add(Therapy(id, userId.toLong(), name, description))
                }
            }
        } catch (e: Exception) {
            // In caso di errore mostriamo comunque la schermata senza bloccare tutto.
            e.printStackTrace()
        }

        renderTherapies()
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
        val age = binding.etAge.text.toString().toIntOrNull()

        if (age == null || age !in 0..130) {
            showError("Inserisci un'età valida (0-130).")
            return
        }

        val sex = if (binding.rbFemale.isChecked) "F" else "M"
        val fields = mapOf("age" to age, "sex" to sex)

        if (userDao.update(user.id, fields)) {
            // Aggiorniamo anche l'utente che abbiamo già in memoria.
            currentUser = user.copy(age = age, sex = sex)
            Snackbar.make(binding.root, "Profilo aggiornato!", Snackbar.LENGTH_SHORT).show()
        } else {
            showError("Errore durante il salvataggio.")
        }
    }

    private fun showAddTherapyDialog() {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val etName = EditText(requireContext())
        etName.hint = "Nome farmaco"

        val etDesc = EditText(requireContext())
        etDesc.hint = "Categoria"

        layout.addView(etName)
        layout.addView(etDesc)

        AlertDialog.Builder(requireContext())
            .setTitle("Nuova Terapia")
            .setView(layout)
            .setPositiveButton("Aggiungi") { _, _ ->
                val name = etName.text.toString().trim()
                val desc = etDesc.text.toString().trim()

                if (name.isNotEmpty()) {
                    val values = ContentValues()
                    values.put("name", name)
                    values.put("description", desc)

                    val id = dbHelper.writableDatabase.insert("therapies", null, values)

                    if (id != -1L) {
                        val userId = currentUser?.id ?: 0
                        therapies.add(Therapy(id, userId.toLong(), name, desc))
                        renderTherapies()
                    }
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun showAddConditionDialog() {
        val etName = EditText(requireContext())
        etName.hint = "Nome condizione"

        val container = LinearLayout(requireContext())
        container.setPadding(50, 40, 50, 10)
        container.addView(
            etName,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Nuova Condizione")
            .setView(container)
            .setPositiveButton("Aggiungi") { _, _ ->
                val name = etName.text.toString().trim()

                if (name.isNotEmpty()) {
                    val userId = currentUser?.id ?: return@setPositiveButton
                    val db = dbHelper.writableDatabase

                    var condId = -1L

                    db.rawQuery(
                        "SELECT id FROM conditions WHERE name = ?",
                        arrayOf(name)
                    ).use { cursor ->
                        if (cursor.moveToFirst()) {
                            condId = cursor.getLong(0)
                        }
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

                        conditions.add(
                            ClinicalCondition(condId, userId.toLong(), name)
                        )
                        renderConditions()
                    }
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun renderTherapies() {
        binding.llTherapies.removeAllViews()

        for (therapy in therapies) {
            val tv = TextView(requireContext())
            tv.text = "${therapy.drugName} - ${therapy.drugCategory}"
            tv.setPadding(0, 8, 0, 8)
            binding.llTherapies.addView(tv)
        }

        if (therapies.isEmpty()) {
            binding.tvNoTherapies.visibility = View.VISIBLE
        } else {
            binding.tvNoTherapies.visibility = View.GONE
        }
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

        if (conditions.isEmpty()) {
            binding.tvNoConditions.visibility = View.VISIBLE
        } else {
            binding.tvNoConditions.visibility = View.GONE
        }
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
