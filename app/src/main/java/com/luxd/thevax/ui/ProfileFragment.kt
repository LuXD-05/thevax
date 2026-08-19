package com.luxd.thevax.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.luxd.thevax.R
import com.luxd.thevax.databinding.FragmentProfileBinding
import com.luxd.thevax.utils.push
import com.luxd.thevax.utils.pop

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        // Usare binding da qui x accedere agli elementi della view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
