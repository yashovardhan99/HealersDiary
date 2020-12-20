package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentChooseActivityBinding
import com.yashovardhan99.healersdiary.utils.Header
import com.yashovardhan99.healersdiary.utils.getIcon
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseActivityFragment : Fragment() {
    private val viewModel: CreateActivityViewModel by activityViewModels()
    private val args: ChooseActivityFragmentArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentChooseActivityBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            Header(getIcon(R.drawable.cross, null, true),
                    args.patientName,
                    null)
        }
        binding.healing.setOnClickListener {
            val action = ChooseActivityFragmentDirections
                    .actionChooseActivityFragmentToNewHealingFragment(args.patientId, args.patientName,
                            args.defaultCharge)
            findNavController().navigate(action)
        }
        binding.payment.setOnClickListener {
            val action = ChooseActivityFragmentDirections
                    .actionChooseActivityFragmentToNewPaymentFragment(args.patientId, args.patientName, args.amountDue)
            findNavController().navigate(action)
        }
        return binding.root
    }
}