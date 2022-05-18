package com.yashovardhan99.healersdiary.onboarding

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.database.OnboardingState
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.MainActivity
import com.yashovardhan99.healersdiary.databinding.FragmentOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

/**
 * Fragment for displaying either Splash screen or Getting started and import options
 * @see OnboardingViewModel
 */
@AndroidEntryPoint
class OnboardingFragment : Fragment() {
    /**
     * View model shared across activity
     */
    private val viewModel: OnboardingViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // data binding
        val binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        // Setting link for disclaimer
        binding.disclaimer.movementMethod = LinkMovementMethod.getInstance()
        lifecycleScope.launchWhenStarted {
            viewModel.onboardingPrefs.collect { onboardingState ->
                when (onboardingState) {
                    OnboardingState.ImportCompleted -> {
                        binding.importOnline.setText(R.string.import_completed)
                        binding.importOnline.isEnabled = false
                    }
                    OnboardingState.Importing -> importOnline()
                    else -> {
                        binding.importOnline.isEnabled = true
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.importRequested.collect { requested ->
                if (requested) importOnline()
            }
        }

        binding.getStarted.setOnClickListener {
            NotificationManagerCompat.from(requireContext())
                .cancel(OnboardingState.IMPORT_COMPLETE_NOTIF_ID)
            viewModel.getStarted()
            startActivity(
                Intent(
                    context,
                    MainActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            requireActivity().finish()
        }
        binding.importOnline.setOnClickListener { importOnline() }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.Onboarding.trackView()
    }

    /**
     * Used to navigate to Import fragment (download module if not available)
     * @see OnboardingViewModel.startImport
     */
    private fun importOnline() {
        viewModel.resetImport()
        findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToImportFirebaseFragment())
    }
}