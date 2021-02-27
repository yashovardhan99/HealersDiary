package com.yashovardhan99.healersdiary.patients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.DashboardViewModel
import com.yashovardhan99.healersdiary.databinding.FragmentHealingListBinding
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.utils.Header.Companion.buildHeader
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.PaymentParent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class PaymentListFragment : Fragment() {
    private val viewModel: PatientDetailViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val args: PaymentListFragmentArgs by navArgs()
    private lateinit var binding: FragmentHealingListBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHealingListBinding.inflate(inflater, container, false)
        viewModel.setPatientId(args.patientId)
        viewModel.patient.asLiveData().observe(viewLifecycleOwner) { patient ->
            binding.header = buildHeader(Icons.Back, resources.getString(R.string.patient_all_payments, patient?.name.orEmpty()), Icons.Add)
        }
        binding.toolbar.icon.setOnClickListener { findNavController().navigateUp() }
        binding.toolbar.optionsIcon.setOnClickListener { dashboardViewModel.newPayment(args.patientId) }
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

    private fun editPayment(payment: PaymentParent.Payment) {
        AnalyticsEvent.Select(AnalyticsEvent.Content.Payment(payment.patientId), AnalyticsEvent.Screen.PaymentLog,
                AnalyticsEvent.SelectReason.Edit).trackEvent()
        // TODO: 31/1/21 Edit Payment
        Toast.makeText(context, R.string.not_yet_implemented, Toast.LENGTH_SHORT).show()
    }

    private fun deletePayment(payment: PaymentParent.Payment) {
        AnalyticsEvent.Select(AnalyticsEvent.Content.Payment(payment.patientId), AnalyticsEvent.Screen.PaymentLog,
                AnalyticsEvent.SelectReason.Delete).trackEvent()
        viewModel.deletePayment(payment.toDatabasePayment())
        Snackbar.make(binding.root, R.string.deleted, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(binding.root.context, R.color.colorSecondary))
                .setAction(R.string.undo) {
                    val done = viewModel.undoDeletePayment()
                    Timber.d("Undo = $done")
                }
                .show()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.PaymentLog.trackView()
    }
}