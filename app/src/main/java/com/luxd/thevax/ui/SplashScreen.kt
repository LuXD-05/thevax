package com.luxd.thevax.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.luxd.thevax.R
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.luxd.thevax.databinding.ActivitySplashScreenBinding
import androidx.fragment.app.FragmentManager

@Suppress("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)

        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1st launch --> auto-switch to Home
        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction().replace(R.id.tab_container, HomeFragment()).commit()

        // Set bottom bar view switching listener
        binding.bottomMenu.setOnItemSelectedListener { tab ->

            // PopBack stack reset (resets view stack when switching tabs)
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

            when (tab.itemId) {
                R.id.home_tab -> supportFragmentManager.beginTransaction().replace(R.id.tab_container, HomeFragment()).commit()
                R.id.record_tab -> supportFragmentManager.beginTransaction().replace(R.id.tab_container, RecordFragment()).commit()
                R.id.profile_tab -> supportFragmentManager.beginTransaction().replace(R.id.tab_container, ProfileFragment()).commit()
            }
            true
        }
    }
}