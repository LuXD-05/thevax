package com.luxd.thevax.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.luxd.thevax.R
import com.luxd.thevax.databinding.DialogVaxBookingRecordBinding
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.Record
import com.luxd.thevax.db.entities.Vaccine
import com.luxd.thevax.db.repositories.RecordRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VaxBookingRecordsDialogFragment(
	private val item: Pair<Vaccine, Record>,
	private val onActionCompleted: () -> Unit
) : DialogFragment() {

	private var _binding: DialogVaxBookingRecordBinding? = null
	private val binding get() = _binding!!

	private val db by lazy { DatabaseHelper(requireContext()) }
	private val repo by lazy { RecordRepository(db) }

	private val calendar = Calendar.getInstance()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = DialogVaxBookingRecordBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		var record = item.second
		calendar.timeInMillis = record.date

		// Set vaccine name & current date & time
		binding.tvVaccineName.text = item.first.name
		binding.tvCurrentDateTime.text = "Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)}"

		val recordCal = Calendar.getInstance().apply {
			timeInMillis = record.date
			set(Calendar.HOUR_OF_DAY, 0)
			set(Calendar.MINUTE, 0)
			set(Calendar.SECOND, 0)
			set(Calendar.MILLISECOND, 0)
		}
		val todayCal = Calendar.getInstance().apply {
			set(Calendar.HOUR_OF_DAY, 0)
			set(Calendar.MINUTE, 0)
			set(Calendar.SECOND, 0)
			set(Calendar.MILLISECOND, 0)
		}
		// If record's date is NOT today or after --> hide btn completed
		if (todayCal.timeInMillis < recordCal.timeInMillis) {
			binding.btnComplete.visibility = View.GONE
		} else {
			binding.btnComplete.visibility = View.VISIBLE
		}

		// On btn edit click
		binding.btnChangeDateTime.setOnClickListener {

			// Pick new date
			DatePickerDialog(requireContext(), { _, y, m, d ->
				calendar.set(Calendar.YEAR, y)
				calendar.set(Calendar.MONTH, m)
				calendar.set(Calendar.DAY_OF_MONTH, d)

				// Pick new time (immediately after date)
				TimePickerDialog(requireContext(), { _, hour, min ->

					// Checks if time is valid (8-12, 14-18)
					if ((hour in 8..11) || (hour in 14..17)) {

						// Updates calendar with new date & time
						calendar.set(Calendar.HOUR_OF_DAY, hour)
						calendar.set(Calendar.MINUTE, min)

						// Updates record in db
						record.date = calendar.timeInMillis
						record.status = "scheduled" // re-puts scheduled status (overwrites missed)
						repo.update(record)

						// Updates dialog UI with new date + current date & time
						calendar.timeInMillis = record.date
						binding.tvCurrentDateTime.text = "Data: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)}"

						Toast.makeText(requireContext(), "Aggiornato", Toast.LENGTH_SHORT).show()

						// Closes dialog
						onActionCompleted.invoke()
						dismiss()
					} else {
						Toast.makeText(requireContext(), "Orario non valido (8-12, 14-18)", Toast.LENGTH_SHORT).show()
					}

				}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

			}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()

		}

		// Set record as completed on btn click
		binding.btnComplete.setOnClickListener {
			record.status = "completed"
			repo.update(record)
			onActionCompleted.invoke()
			dismiss()
		}

		// Deletes record on delete btn click
		binding.btnCancel.setOnClickListener {
			repo.delete(record.id)
			Toast.makeText(requireContext(), "Appuntamento cancellato. Il vaccino è di nuovo disponibile in Home.", Toast.LENGTH_LONG).show()
			onActionCompleted.invoke()
			dismiss()
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

}