package com.yashovardhan99.healersdiary.onboarding

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Fragment for displaying either Splash screen or Getting started and import options
 * @see OnboardingViewModel
 * @see com.yashovardhan99.healersdiary.online.importFirebase.ImportFirebaseFragment
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

        viewModel.onboardingPrefs.observe(viewLifecycleOwner) { preferences ->
            Timber.d("Onboarding pref = $preferences")
            when {
                preferences.importRequest -> {
                    importOnline() //start import
                    binding.disclaimer.visibility = View.VISIBLE
                }
                preferences.onboardingComplete -> { //onboarding is completed/already completed, no need to show any options
                    binding.importOnline.visibility = View.GONE
                    binding.getStarted.visibility = View.GONE
                    binding.disclaimer.visibility = View.GONE
                }
                preferences.importComplete -> { // import is complete -> disable import option
                    binding.getStarted.visibility = View.VISIBLE
                    binding.disclaimer.visibility = View.VISIBLE
                    binding.importOnline.setText(R.string.import_completed)
                    binding.importOnline.isEnabled = false
                    binding.importOnline.visibility = View.VISIBLE
                }
                else -> { //default - all options available
                    binding.importOnline.visibility = View.VISIBLE
                    binding.getStarted.visibility = View.VISIBLE
                    binding.disclaimer.visibility = View.VISIBLE
                }
            }
        }
        binding.getStarted.setOnClickListener {
            viewModel.getStarted()
        }
        binding.importOnline.setOnClickListener { importOnline() }
        return binding.root
    }

    /**
     * Used to navigate to Import fragment (download module if not available)
     * Also resets import preferences using viewModel
     * @see OnboardingViewModel.startImport
     * @see OnboardingViewModel.resetImportPrefs
     */
    private fun importOnline() {
        findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToImportFirebaseFragment())
        viewModel.resetImportPrefs()
    }
}