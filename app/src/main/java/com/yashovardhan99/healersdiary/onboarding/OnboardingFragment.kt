package com.yashovardhan99.healersdiary.onboarding

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
import com.yashovardhan99.core.database.OnboardingState
import com.yashovardhan99.healersdiary.R
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
    val viewModel: OnboardingViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // data binding
        val binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        // Setting link for disclaimer
        binding.disclaimer.movementMethod = LinkMovementMethod.getInstance()

        lifecycleScope.launchWhenStarted {
            viewModel.onboardingPrefs.collect { onboardingState ->
                when (onboardingState) {
                    OnboardingState.ImportCompleted -> {
                        binding.getStarted.visibility = View.VISIBLE
                        binding.disclaimer.visibility = View.VISIBLE
                        binding.importOnline.setText(R.string.import_completed)
                        binding.importOnline.isEnabled = false
                        binding.importOnline.visibility = View.VISIBLE
                    }
                    OnboardingState.OnboardingCompleted -> {
                        binding.importOnline.visibility = View.GONE
                        binding.getStarted.visibility = View.GONE
                        binding.disclaimer.visibility = View.GONE
                    }
                    OnboardingState.OnboardingRequired -> {
                        binding.importOnline.visibility = View.VISIBLE
                        binding.getStarted.visibility = View.VISIBLE
                        binding.disclaimer.visibility = View.VISIBLE
                    }
                    OnboardingState.Importing -> importOnline()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.importRequested.collect { requested ->
                if (requested) importOnline()
            }
        }

        binding.getStarted.setOnClickListener {
            NotificationManagerCompat.from(requireContext()).cancel(OnboardingState.IMPORT_COMPLETE_NOTIF_ID)
            viewModel.getStarted()
        }
        binding.importOnline.setOnClickListener { importOnline() }
        return binding.root
    }

    /**
     * Used to navigate to Import fragment (download module if not available)
     * @see OnboardingViewModel.startImport
     */
    private fun importOnline() {
        findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToImportFirebaseFragment())
    }
}