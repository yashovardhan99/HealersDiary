package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.formatDate
import com.yashovardhan99.core.formatTime
import com.yashovardhan99.core.getLocalDateTimeFromMillis
import com.yashovardhan99.core.toEpochMilli
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.buildHeader
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentNewHealingBinding
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class NewHealingFragment : Fragment() {
    private val args: NewHealingFragmentArgs by navArgs()
    private val viewModel: CreateActivityViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewHealingBinding.inflate(inflater, container, false)
        binding.name.text = args.patientName
        binding.chargeBox.prefixText = NumberFormat.getCurrencyInstance().currency?.symbol ?: ""
        val chargeString =
            BigDecimal(args.defaultCharge).movePointLeft(2).setScale(2, RoundingMode.HALF_EVEN)
                .toPlainString()
        binding.chargeEdit.setText(chargeString)
        binding.chargeEdit.setOnFocusChangeListener { _, hasFocus ->
            val inputText = binding.chargeEdit.text.toString()
            try {
                if (!hasFocus && inputText.isNotBlank()) {
                    val edited =
                        BigDecimal(inputText).setScale(2, RoundingMode.HALF_EVEN).toPlainString()
                    binding.chargeEdit.setText(edited)
                } else if (inputText.isNotBlank() && chargeString == inputText) {
                    binding.chargeEdit.selectAll()
                }
            } catch (e: Exception) {
                Timber.w(e, "Invalid input")
                binding.chargeEdit.text?.clear()
            }
        }
        viewModel.activityTime.asLiveData().observe(viewLifecycleOwner) { time ->
            binding.dateEdit.setText(time.formatDate())
            binding.timeEdit.setText(time.formatTime())
        }
        binding.header = context?.run {
            buildHeader(
                Icons.Back,
                if (viewModel.getHealing().value != null) R.string.edit_healing else R.string.new_healing,
                Icons.Save
            )
        }
        binding.heading.icon.setOnClickListener {
            if (!findNavController().popBackStack()) activity?.finish()
        }

        binding.dateEdit.setOnClickListener { showDatePicker() }
        binding.timeEdit.setOnClickListener { showTimePicker() }
        binding.heading.optionsIcon.setOnClickListener { save(binding) }
        binding.newHealing.setOnClickListener { save(binding) }
        viewModel.error.asLiveData().observe(viewLifecycleOwner) { error ->
            if (error) {
                Snackbar.make(
                    binding.newHealing,
                    resources.getString(R.string.error_creating_activity),
                    Snackbar.LENGTH_LONG
                ).show()
                viewModel.resetError()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.getHealing().collect { healing ->
                if (healing != null) {
                    val chargeStringEdit = BigDecimal(healing.charge).movePointLeft(2)
                        .setScale(2, RoundingMode.HALF_EVEN).toPlainString()
                    binding.chargeEdit.setText(chargeStringEdit)
                    binding.notesEdit.setText(healing.notes)
                    binding.newHealing.setText(R.string.update)
                    binding.header =
                        context?.run { buildHeader(Icons.Back, R.string.edit_healing, Icons.Save) }
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.CreateHealing.trackView()
    }

    private fun save(binding: FragmentNewHealingBinding) {
        val charge = binding.chargeEdit.text.toString()
        val notes = binding.notesEdit.text.toString()
        viewModel.createHealing(charge, notes, args.patientId)
    }

    private fun showTimePicker() {
        val time = viewModel.activityTime.value
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(if (DateFormat.is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setHour(time.hour)
            .setMinute(time.minute)
            .build()
        timePicker.show(childFragmentManager, "timePicker")
        timePicker.addOnPositiveButtonClickListener {
            val pickedDate = viewModel.activityTime.value.toLocalDate()
            val pickedTime = LocalTime.of(timePicker.hour, timePicker.minute)
            val pickedDateTime = LocalDateTime.of(pickedDate, pickedTime)
            val currentTime = LocalDateTime.now()
            Timber.d("Time picked = $pickedTime")
            if (pickedDateTime > currentTime) viewModel.setActivityTime(currentTime)
            else viewModel.setActivityTime(pickedDateTime)
        }
    }

    private fun showDatePicker() {
        val currentDateTime = LocalDateTime.now()
        val constraints = CalendarConstraints.Builder()
            .setEnd(currentDateTime.toEpochMilli())
            .setValidator(DateValidatorPointBackward.now())
            .build()
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraints)
            .setSelection(viewModel.activityTime.value.toEpochMilli())
            .build()
        datePicker.addOnPositiveButtonClickListener {
            val localTime = viewModel.activityTime.value.toLocalTime()
            val localDate = getLocalDateTimeFromMillis(it).toLocalDate()
            val pickedDateTime = LocalDateTime.of(localDate, localTime)
            viewModel.setActivityTime(pickedDateTime)
            showTimePicker()
        }
        datePicker.show(childFragmentManager, "datePicker")
    }
}