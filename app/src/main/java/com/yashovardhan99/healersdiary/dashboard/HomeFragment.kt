package com.yashovardhan99.healersdiary.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentHomeBinding
import com.yashovardhan99.healersdiary.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            Header(getIcon(R.drawable.home),
                    resources.getString(R.string.app_name),
                    getIcon(R.drawable.settings))
        }
        val statAdapter = StatAdapter()
        val headerAdapter = HeaderAdapter(false)
        val activityAdapter = ActivityAdapter()
        val emptyStateAdapter = EmptyStateAdapter(false, EmptyState.DASHBOARD)
        binding.recycler.adapter = ConcatAdapter(statAdapter, headerAdapter, activityAdapter, emptyStateAdapter)
        lifecycleScope.launchWhenStarted {
            viewModel.dashboardFlow.collectLatest { statWithActivity ->
                statAdapter.submitList(statWithActivity.first)
                headerAdapter.isVisible = statWithActivity.second != null
                emptyStateAdapter.isVisible = statWithActivity.second == null
                headerAdapter.notifyDataSetChanged()
                emptyStateAdapter.notifyDataSetChanged()
                activityAdapter.submitList(statWithActivity.second)
            }
        }
        val layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position <= 3) 1
                else 2
            }
        }
        binding.recycler.layoutManager = layoutManager
        return binding.root
    }
}