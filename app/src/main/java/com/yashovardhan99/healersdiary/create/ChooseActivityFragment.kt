package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yashovardhan99.healersdiary.database.ActivityType
import com.yashovardhan99.healersdiary.databinding.FragmentChooseActivityBinding
import com.yashovardhan99.healersdiary.utils.Header.Companion.buildHeader
import com.yashovardhan99.healersdiary.utils.Icons
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseActivityFragment : Fragment() {
    private val viewModel: CreateActivityViewModel by activityViewModels()
    private val args: ChooseActivityFragmentArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentChooseActivityBinding.inflate(inflater, container, false)
        binding.header = buildHeader(Icons.Back, args.patientName)
        binding.heading.icon.setOnClickListener { findNavController().navigateUp() }
        binding.healing.setOnClickListener { newHealing() }
        binding.payment.setOnClickListener { newPayment() }
        viewModel.selectedActivityType.asLiveData().observe(viewLifecycleOwner) { activityType ->
            if (activityType != null) {
                when (activityType) {
                    ActivityType.HEALING -> newHealing()
                    ActivityType.PAYMENT -> newPayment()
                }
                viewModel.resetActivityType()
            }
        }
        return binding.root
    }

    private fun newPayment() {
        val action = ChooseActivityFragmentDirections
                .actionChooseActivityFragmentToNewPaymentFragment(args.patientId, args.patientName, args.amountDue)
        findNavController().navigate(action)
    }

    private fun newHealing() {
        val action = ChooseActivityFragmentDirections
                .actionChooseActivityFragmentToNewHealingFragment(args.patientId, args.patientName,
                        args.defaultCharge)
        findNavController().navigate(action)
    }
}