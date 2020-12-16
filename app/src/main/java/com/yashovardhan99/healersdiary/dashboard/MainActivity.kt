package com.yashovardhan99.healersdiary.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            Timber.d("$controller Dest = $destination args = $arguments")
        }
    }
}