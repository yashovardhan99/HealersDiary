package com.yashovardhan99.healersdiary.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.MaterialElevationScale
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
        val activityAdapter = ActivityAdapter(::goToPatient)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun goToPatient(activity: Activity, view: View) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = transitionDurationLarge
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = transitionDurationLarge
        }
        val patientDetailTransName = resources.getString(R.string.patient_detail_transition)
        val extras = FragmentNavigatorExtras(view to patientDetailTransName)
        val direction = HomeFragmentDirections
                .actionHomeToPatientDetailFragment(activity.patient.id)
        findNavController().navigate(direction, extras)
    }
}