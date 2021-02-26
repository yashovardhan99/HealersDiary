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
import com.yashovardhan99.healersdiary.utils.AnalyticsEvent
import com.yashovardhan99.healersdiary.utils.Header.Companion.buildHeader
import com.yashovardhan99.healersdiary.utils.HealingParent
import com.yashovardhan99.healersdiary.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class HealingListFragment : Fragment() {
    private val viewModel: PatientDetailViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val args: HealingListFragmentArgs by navArgs()
    private lateinit var binding: FragmentHealingListBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHealingListBinding.inflate(inflater, container, false)
        viewModel.setPatientId(args.patientId)
        viewModel.patient.asLiveData().observe(viewLifecycleOwner) { patient ->
            binding.header = buildHeader(Icons.Back, resources.getString(R.string.patient_all_healings, patient?.name.orEmpty()), Icons.Add)
        }
        binding.toolbar.icon.setOnClickListener { findNavController().navigateUp() }
        binding.toolbar.optionsIcon.setOnClickListener { dashboardViewModel.newHealing(args.patientId) }
        binding.recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val adapter = HealingListAdapter(::editHealing, ::deleteHealing)
        lifecycleScope.launchWhenStarted {
            viewModel.getHealings(args.patientId).collect { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        binding.recycler.adapter = adapter
        return binding.root
    }

    private fun editHealing(healing: HealingParent.Healing) {
        AnalyticsEvent.Select(AnalyticsEvent.Content.Healing, AnalyticsEvent.Screen.HealingLog,
                AnalyticsEvent.SelectReason.Edit).trackEvent()
        // TODO: 1/2/21 Edit healing
        Toast.makeText(context, R.string.not_yet_implemented, Toast.LENGTH_SHORT).show()
    }

    private fun deleteHealing(healing: HealingParent.Healing) {
        AnalyticsEvent.Select(AnalyticsEvent.Content.Healing, AnalyticsEvent.Screen.HealingLog,
                AnalyticsEvent.SelectReason.Delete).trackEvent()
        Timber.d("Delete healing $healing")
        viewModel.deleteHealing(healing.toDatabaseHealing())
        Snackbar.make(binding.root, R.string.deleted, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(binding.root.context, R.color.colorSecondary))
                .setAction(R.string.undo) {
                    val done = viewModel.undoDeleteHealing()
                    Timber.d("Undo = $done")
                }
                .show()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.HealingLog.trackView()
    }
}