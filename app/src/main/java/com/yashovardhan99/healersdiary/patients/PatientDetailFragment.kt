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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.MaterialContainerTransform
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.DashboardViewModel
import com.yashovardhan99.healersdiary.database.ActivityType
import com.yashovardhan99.healersdiary.databinding.FragmentPatientDetailBinding
import com.yashovardhan99.healersdiary.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentPatientDetailBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            Timber.d("Setting header")
            Header(getIcon(R.drawable.cross),
                    viewModel.patient.value?.name ?: "Loading!",
                    getIcon(R.drawable.edit, null, true))
        }
        viewModel.setPatientId(args.patientId)
        viewModel.patient.asLiveData().observe(viewLifecycleOwner) { patient ->
            Timber.d("Patient = $patient")
            binding.header = binding.header?.copy(title = patient?.name ?: "")
        }
        val statAdapter = StatAdapter { stat ->
            when (stat.type) {
                ActivityType.HEALING -> goToHealings()
                ActivityType.PAYMENT -> goToPayments()
            }
        }
        val headerAdapter = HeaderAdapter(false)
        val activityAdapter = ActivityAdapter { activity ->
            when (activity.type) {
                is Activity.Type.HEALING -> goToHealings()
                is Activity.Type.PAYMENT -> goToPayments()
            }
        }
        val emptyStatAdapter = EmptyStateAdapter(false, EmptyState.DASHBOARD)
        binding.recycler.adapter = ConcatAdapter(statAdapter, headerAdapter, activityAdapter, emptyStatAdapter)
        binding.recycler.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) = if (position <= 5) 1 else 2
            }
        }
        binding.toolbar.optionsIcon.setOnClickListener {
            Timber.d("Request edit")
            dashboardViewModel.editPatient(args.patientId)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.getStatWithActivities(args.patientId).collect { (stats, activities) ->
                statAdapter.submitList(stats)
                activityAdapter.submitList(activities)
                headerAdapter.isVisible = activities.isNotEmpty()
                headerAdapter.notifyDataSetChanged()
                emptyStatAdapter.isVisible = activities.isEmpty()
                emptyStatAdapter.notifyDataSetChanged()
            }
        }

        return binding.root
    }

    private fun goToHealings() {
        val action = PatientDetailFragmentDirections
                .actionPatientDetailFragmentToHealingListFragment(args.patientId)
        findNavController().navigate(action)
    }

    private fun goToPayments() {
        val action = PatientDetailFragmentDirections
                .actionPatientDetailFragmentToPaymentListFragment(args.patientId)
        findNavController().navigate(action)
    }
}