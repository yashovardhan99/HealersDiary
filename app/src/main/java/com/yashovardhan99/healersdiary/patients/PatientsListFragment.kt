package com.yashovardhan99.healersdiary.patients

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.DashboardViewModel
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.databinding.FragmentPatientsListBinding
import com.yashovardhan99.healersdiary.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class PatientsListFragment : Fragment() {
    private val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPatientsListBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            buildHeader(R.drawable.list, R.string.patients, Icons.CustomButton(R.drawable.add_person, R.string.add_new_patient))
        }
        val patientListAdapter = PatientListAdapter(::openPatientDetail)
        binding.toolbar.optionsIcon.setOnClickListener { viewModel.addNewPatient() }
        val emptyStateAdapter = EmptyStateAdapter(false, EmptyState.PATIENTS)
        binding.recycler.adapter = ConcatAdapter(patientListAdapter, emptyStateAdapter)
        binding.recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        lifecycleScope.launchWhenStarted {
            viewModel.patientsList.collect { patients ->
                patientListAdapter.submitList(patients)
                emptyStateAdapter.isVisible = patients.isEmpty()
                emptyStateAdapter.notifyDataSetChanged()
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.ScreenView(AnalyticsEvent.Screen.PatientList).trackEvent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        viewModel.resetPatientId()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = transitionDurationLarge }
    }

    private fun openPatientDetail(patient: Patient, view: View) {
        AnalyticsEvent.Select(AnalyticsEvent.Content.Patient(patient.id), AnalyticsEvent.Screen.PatientList,
                AnalyticsEvent.SelectReason.Open).trackEvent()
        exitTransition = MaterialElevationScale(false).apply {
            duration = transitionDurationLarge
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = transitionDurationLarge
        }
        val patientDetailTransName = resources.getString(R.string.patient_detail_transition)
        val extras = FragmentNavigatorExtras(view to patientDetailTransName)
        val direction = PatientsListFragmentDirections
                .actionPatientsToPatientDetailFragment(patient.id)
        Timber.d("Patient selected = $patient")
        viewModel.setPatientId(patient.id)
        findNavController().navigate(direction, extras)
    }
}