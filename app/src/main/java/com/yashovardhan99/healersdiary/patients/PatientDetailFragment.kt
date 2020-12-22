package com.yashovardhan99.healersdiary.patients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.DashboardViewModel
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        val statAdapter = StatAdapter()
        val headerAdapter = HeaderAdapter(false)
        val activityAdapter = ActivityAdapter()
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
}