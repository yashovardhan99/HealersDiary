package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.database.ActivityType
import com.yashovardhan99.core.utils.Header.Companion.buildHeader
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentChooseActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseActivityFragment : Fragment() {
    private val viewModel: CreateActivityViewModel by activityViewModels()
    private val args: ChooseActivityFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentChooseActivityBinding.inflate(inflater, container, false)
        binding.header = buildHeader(Icons.Back, args.patientName)
        binding.heading.icon.setOnClickListener {
            if (!findNavController().popBackStack()) activity?.finish()
        }
        binding.healing.setOnClickListener { newHealing(false) }
        binding.payment.setOnClickListener { newPayment(false) }
        viewModel.selectedActivityType.asLiveData().observe(viewLifecycleOwner) { activityType ->
            if (activityType != null) {
                when (activityType) {
                    ActivityType.HEALING -> newHealing(true)
                    ActivityType.PAYMENT -> newPayment(true)
                }
                viewModel.resetActivityType()
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.CreateChooseActivity.trackView()
    }

    private fun newPayment(popUpFlag: Boolean) {
        val action = ChooseActivityFragmentDirections
                .actionChooseActivityFragmentToNewPaymentFragment(args.patientId, args.patientName, args.amountDue)
        findNavController().navigate(action, NavOptions.Builder().setPopUpTo(R.id.chooseActivityFragment, popUpFlag).build())
    }

    private fun newHealing(popUpFlag: Boolean) {
        val action = ChooseActivityFragmentDirections
                .actionChooseActivityFragmentToNewHealingFragment(args.patientId, args.patientName, args.defaultCharge)
        findNavController().navigate(action, NavOptions.Builder().setPopUpTo(R.id.chooseActivityFragment, popUpFlag).build())
    }
}