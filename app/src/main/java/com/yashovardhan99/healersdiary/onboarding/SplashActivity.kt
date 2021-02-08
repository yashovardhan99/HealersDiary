package com.yashovardhan99.healersdiary.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import timber.log.Timber

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private val viewModel: OnboardingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        lifecycleScope.launchWhenStarted {
            delay(1000)
            viewModel.onboardingPrefs.observe(this@SplashActivity) { preferences ->
                Timber.d("Pref at activity = $preferences")
                if (preferences.onboardingComplete && !preferences.importRequest) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish()
                } else if (preferences.importComplete) {
                    findNavController(R.id.nav_host_fragment_container).apply {
                        popBackStack(R.id.onboardingFragment, false)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (intent.getBooleanExtra(OPEN_IMPORT, false)) {
            viewModel.startImport()
        }
    }

    companion object {
        const val OPEN_IMPORT = "open_import"
    }
}