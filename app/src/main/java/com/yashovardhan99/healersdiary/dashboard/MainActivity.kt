package com.yashovardhan99.healersdiary.dashboard

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.ActivityMainBinding
import com.yashovardhan99.healersdiary.utils.Request
import com.yashovardhan99.healersdiary.utils.RequestContract
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: DashboardViewModel by viewModels()
    private val getRequestContract = registerForActivityResult(RequestContract()) { request ->
        Timber.d("Result received; Request = $request")
        if (request != null) handleRequest(request)
    }

    private fun handleRequest(request: Request) {
        when (request) {
            is Request.NewHealing -> TODO()
            is Request.NewPayment -> TODO()
            Request.NewPatient -> getRequestContract.launch(request)
            is Request.NewActivity -> TODO()
            is Request.ViewPatient -> Timber.d("Request: View Patient pid = ${request.patientId}")
            is Request.UpdateHealing -> TODO()
            is Request.UpdatePayment -> TODO()
            is Request.UpdatePatient -> TODO()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            Timber.d("$controller Dest = $destination args = $arguments")
        }
        binding.newRecord.setOnClickListener {
            Timber.d("New record")
            getRequestContract.launch(Request.NewActivity())
        }
    }
}