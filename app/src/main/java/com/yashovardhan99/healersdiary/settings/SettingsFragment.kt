package com.yashovardhan99.healersdiary.settings

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.utils.buildHeader
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.DashboardViewModel
import com.yashovardhan99.healersdiary.databinding.FragmentSettingsBinding
import com.yashovardhan99.healersdiary.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            buildHeader(R.drawable.settings, R.string.settings)
        }
        viewModel.resetPatientId()
        binding.openSource.setOnClickListener {
            startActivity(
                Intent(
                    context,
                    OssLicensesMenuActivity::class.java
                )
            )
        }
        binding.privacyPolicy.setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(resources.getString(R.string.privacy_policy_link))
                }
            )
        }
        binding.eula.setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(resources.getString(R.string.eula_link))
                }
            )
        }
        binding.backup.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsToBackupFragment()
            )
        }
        binding.importFromV1.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.are_you_sure)
                .setMessage(R.string.overrides_all_data)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    Timber.d("Confirming override")
                    val intent = Intent(activity, OnboardingActivity::class.java).apply {
                        putExtra(OnboardingActivity.OPEN_IMPORT, true)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(intent)
                    activity?.finish()
                }.setNegativeButton(R.string.cancel) { dialog: DialogInterface, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        binding.resetAll.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.are_you_sure)
                .setMessage(R.string.delete_warning_message)
                .setPositiveButton(R.string.delete) { _, _ ->
                    Timber.d("Confirming delete")
                    val intent = Intent(activity, OnboardingActivity::class.java).apply {
                        putExtra(OnboardingActivity.CLEAR_ALL, true)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(intent)
                    activity?.finish()
                }.setNegativeButton(R.string.cancel) { dialog: DialogInterface, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.Settings.trackView()
    }
}
