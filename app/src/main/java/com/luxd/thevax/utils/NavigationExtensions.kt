package com.luxd.thevax.utils

import androidx.fragment.app.Fragment
import com.luxd.thevax.R

/**
 * Pushes a fragment in the view stack (Fragment extension function)
 */
fun Fragment.push(target: Fragment) {
    parentFragmentManager.beginTransaction()
        .replace(R.id.tab_container, target)
        .addToBackStack(null) // Fondamentale per tornare indietro col tasto back
        .commit()
}

/**
 * Pops a fragment in the view stack (Fragment extension function)
 */
fun Fragment.pop() {
    parentFragmentManager.popBackStack()
}