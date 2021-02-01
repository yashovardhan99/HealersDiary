package com.yashovardhan99.healersdiary.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.DashboardViewModel
import com.yashovardhan99.healersdiary.databinding.FragmentSettingsBinding
import com.yashovardhan99.healersdiary.utils.buildHeader
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            buildHeader(R.drawable.settings, R.string.settings)
        }
        viewModel.resetPatientId()
        binding.openSource.setOnClickListener { startActivity(Intent(context, OssLicensesMenuActivity::class.java)) }
        binding.privacyPolicy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(resources.getString(R.string.privacy_policy_link))
            })
        }
        binding.eula.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(resources.getString(R.string.eula_link))
            })
        }
        return binding.root
    }
}