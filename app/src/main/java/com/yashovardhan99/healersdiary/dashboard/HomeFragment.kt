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
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.cachedIn
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.transitionDurationLarge
import com.yashovardhan99.core.utils.ActivityLoadStateAdapter
import com.yashovardhan99.core.utils.ActivityParent
import com.yashovardhan99.core.utils.EmptyState
import com.yashovardhan99.core.utils.EmptyStateAdapter
import com.yashovardhan99.core.utils.HeaderAdapter
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.StatAdapter
import com.yashovardhan99.core.utils.buildHeader
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

/**
 * The home page (dashboard) fragment
 * @see DashboardViewModel
 * @see MainActivity
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        // build and attach header
        binding.header = context?.run {
            buildHeader(R.drawable.home, R.string.app_name, Icons.Settings)
        }
        // On settings icon click
        binding.toolbar.optionsIcon.setOnClickListener {
            Timber.d("Options icon press")
            findNavController().navigate(R.id.settings)
        }
        // Creating different adapters
        val statAdapter = StatAdapter()
        val headerAdapter = HeaderAdapter()
        val activityLoadStateAdapter = ActivityLoadStateAdapter()
        val activityAdapter = ActivityAdapter(::goToPatient)
        val emptyStateAdapter = EmptyStateAdapter()
        // concatenating all adapters
        val concatAdapterConfig = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(false)
            .build()
        binding.recycler.adapter =
            ConcatAdapter(
                concatAdapterConfig,
                statAdapter,
                headerAdapter,
                activityAdapter,
                activityLoadStateAdapter,
                emptyStateAdapter
            )
        binding.recycler.recycledViewPool.setMaxRecycledViews(R.layout.activity_card, 20)
        lifecycleScope.launchWhenStarted {
            // collect latest stats and activities
            viewModel.statsFlow.collectLatest { stats ->
                statAdapter.submitList(stats)
            }
        }
        lifecycleScope.launchWhenStarted {
            // collect latest stats and activities
            viewModel.activitiesFlow.cachedIn(this).collectLatest { activities ->
                activityAdapter.submitData(activities)
            }
        }
        lifecycleScope.launchWhenStarted {
            activityAdapter.loadStateFlow.collectLatest { loadStates: CombinedLoadStates ->
                activityLoadStateAdapter.loadState = loadStates.append
                val showEmpty = loadStates.refresh is LoadState.NotLoading &&
                    loadStates.append.endOfPaginationReached &&
                    activityAdapter.itemCount == 0
                headerAdapter.submitList(
                    if (showEmpty) emptyList()
                    else listOf(getString(R.string.recent_activity))
                )
                emptyStateAdapter.submitList(
                    if (showEmpty) listOf(EmptyState.DASHBOARD)
                    else emptyList()
                )
            }
        }
        // Using grid layout to allow stats on top.
        // After first 4 (index 3), we use the full row for each span
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

    /**
     * Go to a particular patient page with animation
     * @param activity The activity the user clicked
     * @param view The view to apply the animation
     */
    private fun goToPatient(activity: ActivityParent, view: View) {
        if (activity !is ActivityParent.Activity) return
        AnalyticsEvent.Select(
            when (activity.type) {
                ActivityParent.Activity.Type.HEALING ->
                    AnalyticsEvent.Content.Healing(activity.patient.id)
                ActivityParent.Activity.Type.PAYMENT ->
                    AnalyticsEvent.Content.Payment(activity.patient.id)
                ActivityParent.Activity.Type.PATIENT ->
                    AnalyticsEvent.Content.Patient(activity.patient.id)
            },
            AnalyticsEvent.Screen.Dashboard, AnalyticsEvent.SelectReason.Open
        ).trackEvent()
        exitTransition = MaterialElevationScale(false).apply {
            duration = transitionDurationLarge
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = transitionDurationLarge
        }
        val patientDetailTransName = resources.getString(R.string.patient_detail_transition)
        val extras = FragmentNavigatorExtras(view to patientDetailTransName)
        viewModel.setPatientId(activity.patient.id)
        val direction = HomeFragmentDirections
            .actionHomeToPatientDetailFragment(activity.patient.id)
        findNavController().navigate(direction, extras)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.Dashboard.trackView()
    }
}
