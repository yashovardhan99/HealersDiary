package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentChoosePatientBinding
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.buildHeader
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
            viewModel.selectPatient(patient)
        }
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        lifecycleScope.launchWhenStarted {
            viewModel.patients.collect {
                adapter.submitList(it)
            }
        }
        viewModel.selectedPatientFlow.asLiveData().observe(viewLifecycleOwner) { patient ->
            if (patient != null) {
                val action = ChoosePatientFragmentDirections.actionChoosePatientFragmentToChooseActivityFragment(
                        patient.id,
                        patient.name,
                        patient.charge,
                        patient.due
                )
                viewModel.selectPatient(null)
                findNavController().navigate(action)
            }
        }
        binding.newPatient.setOnClickListener {
            viewModel.newPatient()
        }
        return binding.root
    }
}