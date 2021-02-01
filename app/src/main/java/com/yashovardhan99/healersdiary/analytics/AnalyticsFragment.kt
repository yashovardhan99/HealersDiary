package com.yashovardhan99.healersdiary.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.DashboardViewModel
import com.yashovardhan99.healersdiary.databinding.FragmentAnalyticsBinding
import com.yashovardhan99.healersdiary.utils.EmptyState
import com.yashovardhan99.healersdiary.utils.buildHeader
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyticsFragment : Fragment() {
    val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            buildHeader(R.drawable.analytics, R.string.analytics)
        }
        binding.emptyState = EmptyState.ANALYTICS
        binding.emptyStateLayout.noDataImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.analytics_illustration, context?.theme))
        viewModel.resetPatientId()
        return binding.root
    }
}