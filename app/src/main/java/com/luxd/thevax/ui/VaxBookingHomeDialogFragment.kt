package com.luxd.thevax.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.luxd.thevax.databinding.DialogVaxBookingHomeBinding
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.Record
import com.luxd.thevax.db.entities.Vaccine
import com.luxd.thevax.db.repositories.RecordRepository
import java.util.Calendar

class VaxBookingHomeDialogFragment(
	private val vaccine: Vaccine,
	private val status: String,
	private val userId: Int,
	private val onBookingConfirmed: () -> Unit
) : DialogFragment() {

	private var _binding: DialogVaxBookingHomeBinding? = null
	private val binding get() = _binding!!

	private val calendar = Calendar.getInstance()
	private var isDateTimeSelected = false

	private val db by lazy { DatabaseHelper(requireContext()) }
	private val recordRepo by lazy { RecordRepository(db) }

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = DialogVaxBookingHomeBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.tvVaccineInfo.text = "Nome: ${vaccine.name}\nTipo: ${vaccine.vaccineType}\nEtà min: ${vaccine.minAge ?: "-"} / max: ${vaccine.maxAge ?: "-"}"
		binding.tvStatus.text = status.uppercase()

		// Colors status label
		val color = when (status) {
			"recommended" -> resources.getColor(android.R.color.holo_green_dark, null)
			"contraindicated" -> resources.getColor(android.R.color.holo_red_dark, null)
			else -> resources.getColor(android.R.color.holo_orange_dark, null)
		}
		binding.tvStatus.setTextColor(color)

		binding.btnPickDateTime.setOnClickListener {
			val now = Calendar.getInstance()
			DatePickerDialog(requireContext(), { _, y, m, d ->
				now.set(Calendar.YEAR, y)
				now.set(Calendar.MONTH, m)
				now.set(Calendar.DAY_OF_MONTH, d)

				TimePickerDialog(requireContext(), { _, hour, min ->
					// Checks if time is valid (8-12, 14-18)
					if ((hour in 8..11) || (hour in 14..17)) {
						now.set(Calendar.HOUR_OF_DAY, hour)
						now.set(Calendar.MINUTE, min)

						calendar.timeInMillis = now.timeInMillis
						isDateTimeSelected = true
						updateDateTimeText()
						binding.btnConfirm.isEnabled = true
					} else {
						Toast.makeText(requireContext(), "Orario non valido (8-12, 14-18). Selezione annullata.", Toast.LENGTH_LONG).show()
					}
				}, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
			}, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
		}

		binding.btnConfirm.setOnClickListener {
			if (!isDateTimeSelected) return@setOnClickListener

			// Adds new record to db
			val newRecord = Record(
				userId = userId,
				vaccineId = vaccine.id,
				status = "scheduled",
				date = calendar.timeInMillis,
				notes = null
			)
			recordRepo.add(newRecord)
			onBookingConfirmed.invoke()
			dismiss()
		}

	}

	private fun updateDateTimeText() {
		if (isDateTimeSelected) binding.tvSelectedDateTime.text = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", calendar)
		else binding.tvSelectedDateTime.text = "Nessuna data/ora selezionata"
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}