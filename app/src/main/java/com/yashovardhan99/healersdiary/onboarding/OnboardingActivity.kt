package com.yashovardhan99.healersdiary.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.yashovardhan99.core.DangerousDatabase
import com.yashovardhan99.core.database.OnboardingState
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

/**
 * Starting Splash screen Activity with onboarding workflow
 * Refer to onStart for description of intent extras
 * @see onStart
 * @see OnboardingFragment
 * @see OnboardingViewModel
 */
@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {
    /**
     * The viewmodel common to fragments
     */
    private val viewModel: OnboardingViewModel by viewModels()

    /**
     * The intent used to start this activity can have 2 boolean extras:-
     * - OPEN_IMPORT Start importing from v1
     * - CLEAR_ALL Clear all data from database and launch fresh
     * @see OPEN_IMPORT
     * @see CLEAR_ALL
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        /**
         * Observing onboarding prefs (saved in a datastore)
         */
        lifecycleScope.launchWhenStarted {
            if (intent.getBooleanExtra(OPEN_IMPORT, false)) {
                viewModel.startImport()
            }
            if (intent.getBooleanExtra(CLEAR_ALL, false)) {
                @OptIn(DangerousDatabase::class)
                viewModel.clearAll()
                val shortcutIds =
                    ShortcutManagerCompat.getDynamicShortcuts(this@OnboardingActivity).map { it.id }
                ShortcutManagerCompat.disableShortcuts(
                    this@OnboardingActivity,
                    shortcutIds,
                    getString(R.string.patient_deleted)
                )
                Snackbar.make(
                    findViewById(R.id.nav_host_fragment_container),
                    R.string.data_cleared,
                    Snackbar.LENGTH_LONG
                ).show()
            }
            viewModel.onboardingPrefs.collect { onboardingState ->
                when (onboardingState) {
                    OnboardingState.ImportCompleted -> {
                        // if import is completed, navigate back to onboarding fragment
                        findNavController(R.id.nav_host_fragment_container).apply {
                            popBackStack(R.id.onboardingFragment, false)
                        }
                    }
                    OnboardingState.OnboardingCompleted -> {
                        Timber.d("Onboarding completed")
                        // If onboarding complete -> launch MainActivity
                        startActivity(
                            Intent(this@OnboardingActivity, MainActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    }
                    else -> {
                    }
                }
            }
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