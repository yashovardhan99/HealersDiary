package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.buildHeader
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentChoosePatientBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ChoosePatientFragment : Fragment() {
    private val viewModel: CreateActivityViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentChoosePatientBinding.inflate(inflater, container, false)
        binding.heading = context?.run {
            buildHeader(Icons.Close, R.string.choose_a_patient)
        }
        binding.header.icon.setOnClickListener {
            if (!findNavController().navigateUp()) activity?.finish()
        }
        val adapter = ChoosePatientAdapter { patient ->
            navigateToChooseActivity(patient, false)
        }
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        lifecycleScope.launchWhenStarted {
            viewModel.patients.collect {
                adapter.submitList(it)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.selectedPatientFlow.collect { patient ->
                viewModel.selectPatient(null)
                if (patient != null) {
                    navigateToChooseActivity(patient, true)
                }
            }
        }
        binding.newPatient.setOnClickListener {
            viewModel.newPatient()
        }
        return binding.root
    }

    private fun navigateToChooseActivity(patient: Patient, popUpFlag: Boolean) {
        val action = ChoosePatientFragmentDirections.actionChoosePatientFragmentToChooseActivityFragment(
                patient.id,
                patient.name,
                patient.charge,
                patient.due
        )
        if (popUpFlag) {
            findNavController().navigate(action,
                    NavOptions.Builder().setPopUpTo(R.id.choosePatientFragment, true).build())
        } else {
            findNavController().navigate(action)
        }
    }
}