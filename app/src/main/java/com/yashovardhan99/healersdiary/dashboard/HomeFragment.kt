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
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentHomeBinding
import com.yashovardhan99.healersdiary.utils.*
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        val statAdapter = StatAdapter().apply { stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY }
        val headerAdapter = HeaderAdapter(false)
        val activityAdapter = ActivityAdapter(::goToPatient).apply { stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY }
        val emptyStateAdapter = EmptyStateAdapter(false, EmptyState.DASHBOARD)
        // concatting all adapters
        binding.recycler.adapter = ConcatAdapter(statAdapter, headerAdapter, activityAdapter, emptyStateAdapter)
        lifecycleScope.launchWhenStarted {
            // collect latest stats and activities
            viewModel.dashboardFlow.collectLatest { (stats, activities) ->
                Timber.d("$activities")
                statAdapter.submitList(stats)
                headerAdapter.isVisible = activities != null
                emptyStateAdapter.isVisible = activities == null
                headerAdapter.notifyDataSetChanged()
                emptyStateAdapter.notifyDataSetChanged()
                activityAdapter.submitList(activities)
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

    /**
     * Go to a particular patient page with animation
     * @param activity The activity the user clicked
     * @param view The view to apply the animation
     */
    private fun goToPatient(activity: ActivityParent, view: View) {
        if (activity !is ActivityParent.Activity) return
        AnalyticsEvent.Select(when (activity.type) {
            ActivityParent.Activity.Type.HEALING -> AnalyticsEvent.Content.Healing(activity.patient.id)
            ActivityParent.Activity.Type.PAYMENT -> AnalyticsEvent.Content.Payment(activity.patient.id)
        }, AnalyticsEvent.Screen.Dashboard, AnalyticsEvent.SelectReason.Open).trackEvent()
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

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.Dashboard.trackView()
    }
}