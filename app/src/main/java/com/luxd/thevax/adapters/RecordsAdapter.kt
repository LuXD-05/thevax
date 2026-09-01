package com.luxd.thevax.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.luxd.thevax.R
import com.luxd.thevax.databinding.ItemRecordBinding
import com.luxd.thevax.db.entities.Record
import com.luxd.thevax.db.entities.Vaccine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RecordsAdapter(
	private val items: List<Pair<Vaccine, Record>>,
	private val onItemClick: (Pair<Vaccine, Record>) -> Unit
) : RecyclerView.Adapter<RecordsAdapter.RecordsViewHolder>() {

	class RecordsViewHolder(val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordsViewHolder {
		val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return RecordsViewHolder(binding)
	}

	override fun onBindViewHolder(holder: RecordsViewHolder, position: Int) {
		val item = items[position]
		val vaccine = item.first
		val record = item.second

		// Set record's vaccine name
		holder.binding.tvVaccineName.text = vaccine.name

		// Set record's date and time
		val cal = Calendar.getInstance()
		cal.timeInMillis = record.date
		holder.binding.tvDay.text = SimpleDateFormat("dd", Locale.getDefault()).format(cal.time)
		holder.binding.tvMonth.text = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time).uppercase()
		holder.binding.tvTime.text = "Ora: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)}"

		// Sets status label + color based on the evaluation
		val (label, color) = when (record.status) {
			"scheduled" -> "Programmato" to R.color.record_scheduled
			"completed" -> "Completato" to R.color.record_completed
			else -> "Saltato" to R.color.record_missed
		}
		holder.binding.tvStatus.text = label
		holder.binding.tvStatus.setTextColor(ContextCompat.getColor(holder.binding.root.context, color))

		// Set onclick handler
		holder.itemView.setOnClickListener { onItemClick(item) }
	}

	override fun getItemCount() = items.size
}