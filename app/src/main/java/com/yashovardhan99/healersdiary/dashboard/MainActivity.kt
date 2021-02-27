package com.yashovardhan99.healersdiary.dashboard

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.ActivityMainBinding
import com.yashovardhan99.core.utils.Request
import com.yashovardhan99.healersdiary.RequestContract
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * The main entry point for the app after SplashActivity
 * Houses the following:-
 * - Dashboard, patients list, analytics and settings
 * - Patient detail page, healing and payment logs
 *
 * Creation pipeline has a different activity
 * @see DashboardViewModel
 * @see HomeFragment
 * @see com.yashovardhan99.healersdiary.patients.PatientsListFragment
 * @see com.yashovardhan99.healersdiary.analytics.AnalyticsFragment
 * @see com.yashovardhan99.healersdiary.settings.SettingsFragment
 * @see com.yashovardhan99.healersdiary.patients.PatientDetailFragment
 * @see com.yashovardhan99.healersdiary.patients.HealingListFragment
 * @see com.yashovardhan99.healersdiary.patients.PaymentListFragment
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var navController: NavController

    /**
     * The request contract registered for this activity.
     * It receives requests for navigation.
     * This also receives back navigation requests such as:-
     * - Navigating to a specific patient page (requested by creation pipeline)
     * @see handleRequest
     * @see Request
     * @see RequestContract
     */
    private val getRequestContract = registerForActivityResult(RequestContract()) { request ->
        Timber.d("Result received; Request = $request")
        if (request != null) handleRequest(request)
    }

    /**
     * Handles a request by navigating to the correct fragment/activity
     * @param request The request to handle
     * @see Request
     * @see RequestContract
     */
    private fun handleRequest(request: Request) {
        Timber.d("Handling request = $request")
        when (request) {
            is Request.NewHealing -> getRequestContract.launch(request)
            is Request.NewPayment -> getRequestContract.launch(request)
            Request.NewPatient -> getRequestContract.launch(request)
            is Request.NewActivity -> getRequestContract.launch(request)
            is Request.ViewPatient -> findNavController(R.id.nav_host_fragment_container)
                    .navigate(request.getUri(), NavOptions.Builder().setLaunchSingleTop(true)
                            .setPopUpTo(R.id.patientDetailFragment, true).build())
            is Request.UpdateHealing -> getRequestContract.launch(request)
            is Request.UpdatePayment -> getRequestContract.launch(request)
            is Request.UpdatePatient -> getRequestContract.launch(request)
            Request.ViewDashboard -> findNavController(R.id.nav_host_fragment_container).popBackStack(R.id.home, false)
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
        // new record -> Launch  new activity with patient id (if a patient page is open)
        binding.newRecord.setOnClickListener {
            Timber.d("New record")
            getRequestContract.launch(Request.NewActivity(viewModel.getPatientId()))
        }
        // internal requests from fragments
        viewModel.requests.asLiveData().observe(this) { request ->
            if (request != null) {
                handleRequest(request)
                viewModel.resetRequest()
            }
        }
    }
}