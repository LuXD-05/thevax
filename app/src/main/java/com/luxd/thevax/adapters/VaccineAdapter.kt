package com.luxd.thevax.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.luxd.thevax.R
import com.luxd.thevax.databinding.ItemVaccineBinding
import com.luxd.thevax.db.entities.Vaccine

class VaccineAdapter(
	private val evaluations: List<Pair<Vaccine, String>>,
	private val onClick: (Vaccine, String) -> Unit
) : RecyclerView.Adapter<VaccineAdapter.VaccineViewHolder>() {

	class VaccineViewHolder(val binding: ItemVaccineBinding) : RecyclerView.ViewHolder(binding.root)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccineViewHolder {
		val binding = ItemVaccineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return VaccineViewHolder(binding)
	}

	override fun onBindViewHolder(holder: VaccineViewHolder, position: Int) {
		val (vaccine, status) = evaluations[position]

		// Sets vaccine info
		holder.binding.tvVaccineName.text = vaccine.name
		holder.binding.tvVaccineType.text = vaccine.vaccineType

		// Sets status label + color based on the evaluation
		val (label, colorRes) = when (status) {
			"recommended" -> "Raccomandato" to R.color.status_recommended
			"contraindicated" -> "Controindicato" to R.color.status_contraindicated
			else -> "Opzionale" to R.color.status_optional
		}
		holder.binding.tvStatus.text = label
		val color = ContextCompat.getColor(holder.binding.root.context, colorRes)
		holder.binding.tvStatus.setTextColor(color)

		// Item click handler
		holder.binding.root.setOnClickListener { onClick(vaccine, status) }
	}

	override fun getItemCount() = evaluations.size
}