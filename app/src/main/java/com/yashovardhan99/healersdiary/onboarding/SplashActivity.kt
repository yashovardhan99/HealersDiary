package com.yashovardhan99.healersdiary.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
                if (preferences.onboardingComplete) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish()
                }
            }
        }
    }
}