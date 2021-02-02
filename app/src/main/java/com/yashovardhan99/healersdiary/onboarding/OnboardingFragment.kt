package com.yashovardhan99.healersdiary.onboarding

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.yashovardhan99.healersdiary.databinding.FragmentOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class OnboardingFragment : Fragment() {
    val viewModel: OnboardingViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        binding.disclaimer.movementMethod = LinkMovementMethod.getInstance()
        viewModel.onboardingPrefs.observe(viewLifecycleOwner) { preferences ->
            Timber.d("Onboarding pref = $preferences")
            if (preferences.onboardingComplete) {
                binding.importOnline.visibility = View.GONE
                binding.getStarted.visibility = View.GONE
            } else {
                binding.importOnline.visibility = View.VISIBLE
                binding.getStarted.visibility = View.VISIBLE
            }
        }
        binding.getStarted.setOnClickListener {
            viewModel.getStarted()
        }
        binding.importOnline.setOnClickListener { importOnline() }
        return binding.root
    }

    fun importOnline() {
        findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToImportFirebaseFragment())
    }
}