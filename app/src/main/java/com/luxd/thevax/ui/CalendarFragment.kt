package com.luxd.thevax.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.luxd.thevax.R
import com.luxd.thevax.databinding.FragmentCalendarBinding
import com.luxd.thevax.utils.push
import com.luxd.thevax.utils.pop

class CalendarFragment : Fragment(R.layout.fragment_calendar) {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCalendarBinding.bind(view)

        // Usare binding da qui x accedere agli elementi della view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}