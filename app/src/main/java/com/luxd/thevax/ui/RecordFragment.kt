package com.luxd.thevax.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.luxd.thevax.R
import com.luxd.thevax.adapters.RecordsAdapter
import com.luxd.thevax.databinding.FragmentRecordsBinding
import com.luxd.thevax.db.DatabaseHelper
import com.luxd.thevax.db.entities.User
import com.luxd.thevax.db.repositories.RecordRepository
import com.luxd.thevax.db.repositories.VaccineRepository
import com.luxd.thevax.services.SessionService
import java.util.Calendar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RecordFragment : Fragment(R.layout.fragment_records) {
    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { DatabaseHelper(requireContext()) }
    private val recordRepo by lazy { RecordRepository(db) }
    private val vaccineRepo by lazy { VaccineRepository(db) }

    private lateinit var currentUser: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecordsBinding.bind(view)

        refreshData()
    }

    private fun refreshData() {
        viewLifecycleOwner.lifecycleScope.launch {

            // Fetch user
            currentUser = SessionService.getInstance().getUser() ?: return@launch logout()

            // Gets calendar instance
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val todayStart = cal.timeInMillis

            // Updates missed records with 'missed' status (scheduled & not completed the day after)
            recordRepo.markMissedRecords(currentUser.id, todayStart)

            // Fetch user' records (& display tv if empty)
            val records = recordRepo.getRecordsForUser(currentUser.id)
            if (records.isEmpty()) {
                binding.rvRecords.visibility = View.GONE
                binding.tvEmptyRecords.visibility = View.VISIBLE
                return@launch
            }

            // Set rv visible (overwrites no records)
            binding.rvRecords.visibility = View.VISIBLE
            binding.tvEmptyRecords.visibility = View.GONE

            // Gets all vaccines
            val vaccines = vaccineRepo.getAll()

            // Composes Pairs of <Vaccine, Record> for each record (since dialog needs vax info)
            val items = records.mapNotNull { record ->
                // Gets vaccine for record (if exists, otherwise null)
                val vaccine = vaccines.find { v -> v.id == record.vaccineId }
                // Pair it with record
                if (vaccine != null) Pair(vaccine, record)
                else null
            }.sortedBy { it.second.date }

            // Setup rv adapter
            binding.rvRecords.layoutManager = LinearLayoutManager(requireContext())
            binding.rvRecords.adapter = RecordsAdapter(items) { item ->
                // Opens dialog
                val dialog = VaxBookingRecordsDialogFragment(item) {
                    refreshData()
                }
                dialog.show(parentFragmentManager, "VaxBookingRecordsDialog")
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