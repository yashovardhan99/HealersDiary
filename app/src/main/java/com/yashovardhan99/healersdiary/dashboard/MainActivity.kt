package com.yashovardhan99.healersdiary.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.yashovardhan99.core.database.OnboardingState
import com.yashovardhan99.core.utils.PatientProfileDrawable
import com.yashovardhan99.core.utils.Request
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.RequestContract
import com.yashovardhan99.healersdiary.databinding.ActivityMainBinding
import com.yashovardhan99.healersdiary.onboarding.OnboardingActivity
import com.yashovardhan99.healersdiary.onboarding.OnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

/**
 * The main entry point for the app after OnboardingActivity
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

private const val FLAG_ONBOARDING_COMPLETED = 1
private const val FLAG_PAGE_LOADED = 1 shl 1
private const val FLAG_HIDE_SPLASH = FLAG_ONBOARDING_COMPLETED or FLAG_PAGE_LOADED

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: DashboardViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private lateinit var navController: NavController
    private var showSplash = 0

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
                .navigate(
                    request.getUri(),
                    NavOptions.Builder().setLaunchSingleTop(true)
                        .setPopUpTo(R.id.patientDetailFragment, true).build()
                )
            is Request.UpdateHealing -> getRequestContract.launch(request)
            is Request.UpdatePayment -> getRequestContract.launch(request)
            is Request.UpdatePatient -> getRequestContract.launch(request)
            Request.ViewDashboard -> findNavController(R.id.nav_host_fragment_container)
                .popBackStack(R.id.home, false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { showSplash != FLAG_HIDE_SPLASH }
        lifecycleScope.launchWhenCreated {
            onboardingViewModel.onboardingPrefs.collect { onboardingState ->
                when (onboardingState) {
                    OnboardingState.OnboardingCompleted -> showSplash =
                        showSplash or FLAG_ONBOARDING_COMPLETED
                    OnboardingState.Fetching -> {}
                    else -> {
                        startActivity(
                            Intent(this@MainActivity, OnboardingActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                        finish()
                    }
                }
            }
        }

        // If no dynamic shortcuts are there, request some
        if (ShortcutManagerCompat.getDynamicShortcuts(this).isEmpty()) {
            viewModel.requestShortcuts(ShortcutManagerCompat.getMaxShortcutCountPerActivity(this))
        }

        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)
                    as NavHostFragment
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
        lifecycleScope.launchWhenCreated {
            viewModel.shortcuts.collect {
                val px = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 120f, resources.displayMetrics
                ).toInt()
                val icon = IconCompat.createWithAdaptiveBitmap(
                    PatientProfileDrawable(it.label).toBitmap(px, px)
                )
                val intent = Intent(Intent.ACTION_VIEW)
                    .setClass(this@MainActivity, this@MainActivity::class.java)
                    .setData(Request.ViewPatient(it.patientId).getUri())
                val person = Person.Builder()
                    .setName(it.label)
                    .setKey("patient_${it.patientId}")
                    .setBot(false)
                    .setIcon(icon)
                    .build()
                val shortcutInfo =
                    ShortcutInfoCompat.Builder(this@MainActivity, "patient_${it.patientId}")
                        .setShortLabel(it.label)
                        .setPerson(person)
                        .setIcon(icon)
                        .setIntent(intent)
                        .setRank(it.rank)
                        .addCapabilityBinding(
                            "actions.intent.GET_THING",
                            "thing.name",
                            listOf(it.label)
                        )
                        .build()
                ShortcutManagerCompat.pushDynamicShortcut(this@MainActivity, shortcutInfo)
            }
        }

        // Handle intents
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let {
                val request = Request.fromUri(it, intent.extras)
                getRequestContract.launch(request)
                intent = intent.setData(null).setAction(null)
            }
            showSplash = showSplash or FLAG_PAGE_LOADED
        } else if (intent.action == Intent.ACTION_INSERT) {
            intent.data?.let {
                val request = Request.fromUri(it, intent.extras)
                getRequestContract.launch(request)
                intent = intent.setData(null).setAction(null)
            }
        } else {
            showSplash = showSplash or FLAG_PAGE_LOADED
        }
    }
}
