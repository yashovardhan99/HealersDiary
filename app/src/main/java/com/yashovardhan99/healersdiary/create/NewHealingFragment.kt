package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.navArgs
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentNewHealingBinding
import com.yashovardhan99.healersdiary.utils.DatePickerFragment
import com.yashovardhan99.healersdiary.utils.Header
import com.yashovardhan99.healersdiary.utils.TimePickerFragment
import com.yashovardhan99.healersdiary.utils.getIcon
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat

@AndroidEntryPoint
class NewHealingFragment : Fragment() {
    private val args: NewHealingFragmentArgs by navArgs()
    private val viewModel: CreateActivityViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentNewHealingBinding.inflate(inflater, container, false)
        binding.name.text = args.patientName
        binding.chargeBox.prefixText = NumberFormat.getCurrencyInstance().currency?.symbol ?: ""
        val chargeString = BigDecimal(args.defaultCharge).movePointLeft(2).setScale(2, RoundingMode.HALF_EVEN).toPlainString()
        binding.chargeEdit.setText(chargeString)
        binding.chargeEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val edited = BigDecimal(binding.chargeEdit.text.toString()).setScale(2, RoundingMode.HALF_EVEN).toPlainString()
                binding.chargeEdit.setText(edited)
            } else if (chargeString == binding.chargeEdit.text?.toString()) {
                binding.chargeEdit.selectAll()
            }
        }
        viewModel.activityCalendar.asLiveData().observe(viewLifecycleOwner) { calendar ->
            binding.dateEdit.setText(SimpleDateFormat.getDateInstance().format(calendar.time))
            binding.timeEdit.setText(SimpleDateFormat.getTimeInstance().format(calendar.time))
        }
        binding.header = context?.run {
            Header(getIcon(R.drawable.cross, null, true),
                    getString(R.string.new_healing),
                    getIcon(R.drawable.save, getString(R.string.save), true))
        }

        val timePicker = TimePickerFragment { calendar ->
            viewModel.setActivityCalendar(calendar)
            Timber.d("Selected time = $calendar")
        }
        val datePicker = DatePickerFragment { calendar ->
            viewModel.setActivityCalendar(calendar)
            Timber.d("Selected = $calendar")
            showTimePicker(timePicker)
        }
        binding.dateEdit.setOnClickListener {
            datePicker.arguments = bundleOf(Pair(DatePickerFragment.DateKey,
                    viewModel.activityCalendar.value.timeInMillis))
            datePicker.show(parentFragmentManager, "datePicker")
        }
        binding.timeEdit.setOnClickListener { showTimePicker(timePicker) }
        return binding.root
    }

    private fun showTimePicker(timePicker: TimePickerFragment) {
        timePicker.arguments = bundleOf(Pair(TimePickerFragment.TimeKey,
                viewModel.activityCalendar.value.timeInMillis))
        timePicker.show(parentFragmentManager, "timePicker")
    }
}