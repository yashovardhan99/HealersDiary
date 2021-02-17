package com.yashovardhan99.healersdiary.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.MainActivity
import com.yashovardhan99.healersdiary.utils.DangerousDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Starting Splash screen Activity with onboarding workflow
 * Refer to onStart for description of intent extras
 * @see onStart
 * @see OnboardingFragment
 * @see OnboardingViewModel
 * @see com.yashovardhan99.healersdiary.online.importFirebase.ImportFirebaseFragment
 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    /**
     * The viewmodel common to fragments
     */
    private val viewModel: OnboardingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        /**
         * Observing onboarding prefs (saved in a datastore)
         */
        lifecycleScope.launchWhenStarted {
            // Delay for 1 second splash screen
            delay(1000)
            viewModel.onboardingPrefs.observe(this@SplashActivity) { preferences ->
                Timber.d("Pref at activity = $preferences")
                // If onboarding complete -> launch MainActivity
                if (preferences.onboardingComplete && !preferences.importRequest) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish()
                } else if (preferences.importComplete) {
                    // if import is completed, navigate back to onboarding fragment
                    findNavController(R.id.nav_host_fragment_container).apply {
                        popBackStack(R.id.onboardingFragment, false)
                    }
                }
            }
        }
    }

    /**
     * The intent used to start this activity can have 2 boolean extras:-
     * - OPEN_IMPORT Start importing from v1
     * - CLEAR_ALL Clear all data from database and launch fresh
     * @see OPEN_IMPORT
     * @see CLEAR_ALL
     */
    override fun onStart() {
        super.onStart()
        if (intent.getBooleanExtra(OPEN_IMPORT, false)) {
            viewModel.startImport()
        }
        if (intent.getBooleanExtra(CLEAR_ALL, false)) {
            @OptIn(DangerousDatabase::class)
            viewModel.clearAll()
            Snackbar.make(findViewById(R.id.nav_host_fragment_container), R.string.data_cleared, Snackbar.LENGTH_LONG).show()
        }
    }

    companion object {
        /**
         * Extra key, used to start importing from v1 on launch
         */
        const val OPEN_IMPORT = "open_import"

        /**
         * Extra key, used to clear all data
         */
        const val CLEAR_ALL = "clear_all"
    }
}