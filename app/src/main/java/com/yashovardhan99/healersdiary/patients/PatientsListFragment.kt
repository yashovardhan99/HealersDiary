package com.yashovardhan99.healersdiary.patients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.DashboardViewModel
import com.yashovardhan99.healersdiary.databinding.FragmentPatientsListBinding
import com.yashovardhan99.healersdiary.utils.EmptyState
import com.yashovardhan99.healersdiary.utils.EmptyStateAdapter
import com.yashovardhan99.healersdiary.utils.Header
import com.yashovardhan99.healersdiary.utils.getIcon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class PatientsListFragment : Fragment() {
    private val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPatientsListBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            Header(getIcon(R.drawable.list),
                    resources.getString(R.string.patients),
                    getIcon(R.drawable.add_person, null, true))
        }
        val patientListAdapter = PatientListAdapter {
            Timber.d("Patient selected = $it")
            viewModel.viewPatient(it.id)
        }
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
}