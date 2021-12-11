package com.yashovardhan99.healersdiary.patients

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.MaterialContainerTransform
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.database.ActivityType
import com.yashovardhan99.core.getColorFromAttr
import com.yashovardhan99.core.transitionDurationLarge
import com.yashovardhan99.core.utils.ActivityParent
import com.yashovardhan99.core.utils.EmptyState
import com.yashovardhan99.core.utils.EmptyStateAdapter
import com.yashovardhan99.core.utils.Header.Companion.buildHeader
import com.yashovardhan99.core.utils.HeaderAdapter
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.StatAdapter
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.ActivityAdapter
import com.yashovardhan99.healersdiary.dashboard.DashboardViewModel
import com.yashovardhan99.healersdiary.databinding.FragmentPatientDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
class PatientDetailFragment : Fragment() {
    private val args: PatientDetailFragmentArgs by navArgs()
    val viewModel: PatientDetailViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_container
            duration = transitionDurationLarge
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().getColorFromAttr(R.attr.colorSurface))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPatientDetailBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            buildHeader(
                Icons.Close, getString(R.string.loading),
                Icons.CustomButton(R.drawable.edit, R.string.edit_patient)
            )
        }
        binding.toolbar.icon.setOnClickListener { findNavController().navigateUp() }
        viewModel.setPatientId(args.patientId)
        dashboardViewModel.setPatientId(args.patientId)
        viewModel.patient.asLiveData().observe(viewLifecycleOwner) { patient ->
            Timber.d("Patient = $patient")
            binding.header = buildHeader(
                Icons.Close, patient?.name ?: "",
                Icons.CustomButton(R.drawable.edit, R.string.edit_patient)
            )
        }
        val statAdapter = StatAdapter { stat ->
            when (stat.type) {
                ActivityType.HEALING -> goToHealings()
                ActivityType.PAYMENT -> goToPayments()
                ActivityType.PATIENT -> Unit
            }
        }
        val headerAdapter = HeaderAdapter()
        val activityAdapter = ActivityAdapter(true, { activity, _ ->
            if (activity !is ActivityParent.Activity) return@ActivityAdapter
            when (activity.type) {
                ActivityParent.Activity.Type.HEALING -> goToHealings()
                ActivityParent.Activity.Type.PAYMENT -> goToPayments()
                ActivityParent.Activity.Type.PATIENT -> Unit
            }
        })
        val emptyStateAdapter = EmptyStateAdapter()
        val concatAdapterConfig = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(false)
            .build()
        binding.recycler.adapter =
            ConcatAdapter(
                concatAdapterConfig, statAdapter, headerAdapter, activityAdapter, emptyStateAdapter
            )
        binding.recycler.layoutManager =
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) = if (position <= 5) 1 else 2
                }
            }
        binding.toolbar.optionsIcon.setOnClickListener {
            AnalyticsEvent.Select(
                AnalyticsEvent.Content.Patient(args.patientId), AnalyticsEvent.Screen.PatientDetail,
                AnalyticsEvent.SelectReason.Edit
            ).trackEvent()
            Timber.d("Request edit")
            dashboardViewModel.editPatient(args.patientId)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.activities.collect { activities ->
                activityAdapter.submitData(activities)
            }
        }
        lifecycleScope.launchWhenStarted {
            activityAdapter.loadStateFlow.collectLatest { loadStates: CombinedLoadStates ->
                // activityLoadStateAdapter.loadState = loadStates.append
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

        lifecycleScope.launchWhenStarted {
            viewModel.stats.collectLatest { stats ->
                statAdapter.submitList(stats)
            }
        }

        return binding.root
    }

    private fun goToHealings() {
        AnalyticsEvent.Select(
            AnalyticsEvent.Content.Healing(args.patientId), AnalyticsEvent.Screen.PatientDetail,
            AnalyticsEvent.SelectReason.Open
        ).trackEvent()
        val action = PatientDetailFragmentDirections
            .actionPatientDetailFragmentToHealingListFragment(args.patientId)
        findNavController().navigate(action)
    }

    private fun goToPayments() {
        AnalyticsEvent.Select(
            AnalyticsEvent.Content.Payment(args.patientId), AnalyticsEvent.Screen.PatientDetail,
            AnalyticsEvent.SelectReason.Open
        ).trackEvent()
        val action = PatientDetailFragmentDirections
            .actionPatientDetailFragmentToPaymentListFragment(args.patientId)
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.PatientDetail.trackView()
    }
}
