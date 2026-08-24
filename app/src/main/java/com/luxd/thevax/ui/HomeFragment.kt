package com.luxd.thevax.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.luxd.thevax.R
import com.luxd.thevax.databinding.FragmentHomeBinding
import com.luxd.thevax.utils.push
import com.luxd.thevax.utils.pop

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    /*
    TODO:
        (onViewCreated)
        - fetch top card's user data (user, therapies, conditions...)
        - fetch vaccines (with user's data) + evaluation + draw to screen (or show no vaccines)
        (onRecyclerViewItemClick)
        - show vaccine's details (Fragment.push extension)
    */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // Usare binding da qui x accedere agli elementi della view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}