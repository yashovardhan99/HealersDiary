package com.yashovardhan99.healersdiary.dashboard

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
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
    private lateinit var navController: NavController
    private val getRequestContract = registerForActivityResult(RequestContract()) { request ->
        Timber.d("Result received; Request = $request")
        if (request != null) handleRequest(request)
    }

    private fun handleRequest(request: Request) {
        Timber.d("Handling request = $request")
        when (request) {
            is Request.NewHealing -> getRequestContract.launch(request)
            is Request.NewPayment -> getRequestContract.launch(request)
            Request.NewPatient -> getRequestContract.launch(request)
            is Request.NewActivity -> getRequestContract.launch(request)
            is Request.ViewPatient -> findNavController(R.id.nav_host_fragment_container).navigate(request.getUri())
            is Request.UpdateHealing -> getRequestContract.launch(request)
            is Request.UpdatePayment -> getRequestContract.launch(request)
            is Request.UpdatePatient -> getRequestContract.launch(request)
            Request.ViewDashboard -> findNavController(R.id.nav_host_fragment_container).navigate(R.id.home)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            Timber.d("$controller Dest = $destination args = $arguments")
        }
        binding.newRecord.setOnClickListener {
            Timber.d("New record")
            getRequestContract.launch(Request.NewActivity())
        }
        viewModel.requests.asLiveData().observe(this) { request ->
            if (request != null) {
                handleRequest(request)
                viewModel.resetRequest()
            }
        }
    }
}