package com.luxd.thevax.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
		val (label, color) = when (status) {
			"recommended" -> "Raccomandato" to Color.parseColor("#2E7D32")
			"contraindicated" -> "Controindicato" to Color.parseColor("#C62828")
			else -> "Opzionale" to Color.parseColor("#EF6C00")
		}
		holder.binding.tvStatus.text = label
		holder.binding.tvStatus.setTextColor(color)

		// Item click handler
		holder.binding.root.setOnClickListener { onClick(vaccine, status) }
	}

	override fun getItemCount() = evaluations.size
}