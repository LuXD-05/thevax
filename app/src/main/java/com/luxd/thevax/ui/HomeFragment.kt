package com.luxd.thevax.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.luxd.thevax.R
import com.luxd.thevax.databinding.FragmentHomeBinding
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.User
import com.luxd.thevax.db.repositories.UserRepository
import com.luxd.thevax.db.repositories.VaccineRepository
import com.luxd.thevax.services.SessionService
import com.luxd.thevax.utils.push
import com.luxd.thevax.utils.pop
import androidx.recyclerview.widget.LinearLayoutManager
import com.luxd.thevax.adapters.VaccineAdapter
import com.luxd.thevax.db.entities.ClinicalCondition
import com.luxd.thevax.db.entities.Therapy

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { DatabaseHelper(requireContext()) }
    private val userRepo by lazy { UserRepository(db) }
    private val vaccineRepo by lazy { VaccineRepository(db) }

    private lateinit var currentUser: User
    private lateinit var userTherapy: Therapy
    private var userConditions = mutableListOf<ClinicalCondition>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Fetch user data
        currentUser = SessionService.getInstance().getUser() ?: return logout()
        userTherapy = userRepo.getTherapyForUser(currentUser.id)
        userConditions = userRepo.getConditionsForUser(currentUser.id).toMutableList()

        // Update header card
        binding.tvName.text = "${currentUser.firstName} ${currentUser.lastName}"
        binding.tvAge.text = "Età: ${currentUser.age} | Sesso: ${currentUser.sex}"
        binding.tvTherapies.text = "Terapia: ${userTherapy?.name ?: "Nessuna"}"
        
        val conditionsText = if (userConditions.isEmpty()) "Nessuna" else userConditions.joinToString(", ") { it.conditionName }
        binding.tvConditions.text = "Condizioni: $conditionsText"

        // Fetch vaccines evaluations for user
        val evaluations = vaccineRepo.evaluateVaccinesForUser(currentUser, userConditions)

        // Updates counters
        binding.tvRecCount.text = evaluations.count { it.second == "recommended" }.toString()
        binding.tvOptCount.text = evaluations.count { it.second == "optional" }.toString()
        binding.tvCntCount.text = evaluations.count { it.second == "contraindicated" }.toString()

        // Draws list (or no vaccines)
        if (evaluations.isEmpty()) {
            binding.rvVaccines.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
        } else {
            binding.rvVaccines.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE

            // Setup vaccines recycler view
            binding.rvVaccines.layoutManager = LinearLayoutManager(requireContext())
            binding.rvVaccines.adapter = VaccineAdapter(evaluations) { vaccine, status ->
                // Opens vaccine detail page //TODO: create VaxDetailFragment
                //push(VaccineDetailFragment.newInstance(vaccine, status))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logout() {
        // Clears user in session
        SessionService.getInstance().clear()
        // Goes to login + clear backStack
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}