package com.yashovardhan99.healersdiary.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentAnalyticsBinding
import com.yashovardhan99.healersdiary.utils.EmptyState
import com.yashovardhan99.healersdiary.utils.Header
import com.yashovardhan99.healersdiary.utils.getIcon

class AnalyticsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            Header(getIcon(R.drawable.analytics),
                    resources.getString(R.string.analytics),
                    getIcon(R.drawable.settings)
            )
        }
        binding.emptyState = EmptyState.ANALYTICS
        binding.emptyStateLayout.noDataImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.analytics_illustration, context?.theme))
        return binding.root
    }
}