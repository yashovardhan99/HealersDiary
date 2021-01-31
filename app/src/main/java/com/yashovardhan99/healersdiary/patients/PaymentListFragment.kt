package com.yashovardhan99.healersdiary.patients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.database.Payment
import com.yashovardhan99.healersdiary.databinding.FragmentHealingListBinding
import com.yashovardhan99.healersdiary.utils.Header
import com.yashovardhan99.healersdiary.utils.getIcon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PaymentListFragment : Fragment() {
    private val viewModel: PatientDetailViewModel by activityViewModels()
    private val args: PaymentListFragmentArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentHealingListBinding.inflate(inflater, container, false)
        viewModel.setPatientId(args.patientId)
        viewModel.patient.asLiveData().observe(viewLifecycleOwner) { patient ->
            binding.header = context?.run {
                Header(getIcon(R.drawable.back, null, true),
                        resources.getString(R.string.patient_all_payments,
                                patient?.name.orEmpty()),
                        null)
            }
        }
        binding.toolbar.icon.setOnClickListener { findNavController().navigateUp() }
        binding.recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val adapter = PaymentListAdapter(::editPayment, ::deletePayment)

        lifecycleScope.launchWhenStarted {
            viewModel.getPayments(args.patientId).collect { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        binding.recycler.adapter = adapter
        return binding.root
    }

    private fun editPayment(payment: Payment) {
        // TODO: 31/1/21 Edit Payment
    }

    private fun deletePayment(payment: Payment) {
        viewModel.deletePayment(payment)
    }
}