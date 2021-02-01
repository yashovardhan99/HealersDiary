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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentHomeBinding
import com.yashovardhan99.healersdiary.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            buildHeader(R.drawable.home, R.string.app_name, Icons.Settings)
        }
        binding.toolbar.optionsIcon.setOnClickListener {
            Timber.d("Options icon press")
            findNavController().navigate(R.id.settings)
        }
        val statAdapter = StatAdapter().apply { stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY }
        val headerAdapter = HeaderAdapter(false)
        val activityAdapter = ActivityAdapter(::goToPatient).apply { stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY }
        val emptyStateAdapter = EmptyStateAdapter(false, EmptyState.DASHBOARD)
        binding.recycler.adapter = ConcatAdapter(statAdapter, headerAdapter, activityAdapter, emptyStateAdapter)
        lifecycleScope.launchWhenStarted {
            viewModel.dashboardFlow.collectLatest { statWithActivity ->
                Timber.d("${statWithActivity.second}")
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
        Timber.d("Setting up thumbfastscroller")
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = transitionDurationLarge }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        viewModel.resetPatientId()
    }

    private fun goToPatient(activity: ActivityParent, view: View) {
        if (activity !is ActivityParent.Activity) return
        exitTransition = MaterialElevationScale(true).apply {
            duration = transitionDurationLarge
        }
        reenterTransition = MaterialElevationScale(false).apply {
            duration = transitionDurationLarge
        }
        val patientDetailTransName = resources.getString(R.string.patient_detail_transition)
        val extras = FragmentNavigatorExtras(view to patientDetailTransName)
        viewModel.setPatientId(activity.patient.id)
        val direction = HomeFragmentDirections
                .actionHomeToPatientDetailFragment(activity.patient.id)
        findNavController().navigate(direction, extras)
    }
}