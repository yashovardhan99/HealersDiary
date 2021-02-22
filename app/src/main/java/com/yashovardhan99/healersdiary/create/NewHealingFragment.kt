package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentNewHealingBinding
import com.yashovardhan99.healersdiary.utils.Icons
import com.yashovardhan99.healersdiary.utils.buildHeader
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

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
            val inputText = binding.chargeEdit.text.toString()
            try {
                if (!hasFocus && inputText.isNotBlank()) {
                    val edited = BigDecimal(inputText).setScale(2, RoundingMode.HALF_EVEN).toPlainString()
                    binding.chargeEdit.setText(edited)
                } else if (inputText.isNotBlank() && chargeString == inputText) {
                    binding.chargeEdit.selectAll()
                }
            } catch (e: Exception) {
                Timber.w(e, "Invalid input")
                binding.chargeEdit.text?.clear()
            }
        }
        viewModel.activityCalendar.asLiveData().observe(viewLifecycleOwner) { calendar ->
            binding.dateEdit.setText(SimpleDateFormat.getDateInstance().format(calendar.time))
            binding.timeEdit.setText(SimpleDateFormat.getTimeInstance().format(calendar.time))
        }
        binding.header = context?.run {
            buildHeader(Icons.Back, R.string.new_healing, Icons.Save)
        }
        binding.heading.icon.setOnClickListener { findNavController().navigateUp() }

        binding.dateEdit.setOnClickListener { showDatePicker() }
        binding.timeEdit.setOnClickListener { showTimePicker() }
        binding.heading.optionsIcon.setOnClickListener { save(binding) }
        binding.newHealing.setOnClickListener { save(binding) }
        viewModel.error.asLiveData().observe(viewLifecycleOwner) { error ->
            if (error) {
                Snackbar.make(binding.newHealing, resources.getString(R.string.error_creating_activity), Snackbar.LENGTH_LONG)
                viewModel.resetError()
            }
        }
        return binding.root
    }

    private fun save(binding: FragmentNewHealingBinding) {
        val charge = binding.chargeEdit.text.toString()
        val notes = binding.notesEdit.text.toString()
        viewModel.createHealing(charge, notes, args.patientId)
    }

    private fun showTimePicker() {
        val calendar = viewModel.activityCalendar.value
        val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(if (DateFormat.is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE))
                .build()
        timePicker.show(childFragmentManager, "timePicker")
        timePicker.addOnPositiveButtonClickListener {
            val setCalendar = Calendar.getInstance().apply {
                timeInMillis = viewModel.activityCalendar.value.timeInMillis
                set(Calendar.HOUR_OF_DAY, timePicker.hour)
                set(Calendar.MINUTE, timePicker.minute)
                set(Calendar.SECOND, 0)
            }
            val cur = Calendar.getInstance()
            Timber.d("Calendar set = ${setCalendar.time}")
            if (setCalendar > cur) viewModel.setActivityCalendar(cur)
            else viewModel.setActivityCalendar(setCalendar)
        }
    }

    private fun showDatePicker() {
        val constraints = CalendarConstraints.Builder()
                .setEnd(Calendar.getInstance().timeInMillis)
                .setValidator(DateValidatorPointBackward.now())
                .build()
        val setCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            val calendar = viewModel.activityCalendar.value
            set(Calendar.DATE, calendar.get(Calendar.DATE))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        }
        val datePicker = MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(constraints)
                .setSelection(setCalendar.timeInMillis)
                .build()
        datePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    .apply { timeInMillis = it }
            viewModel.setActivityCalendar(Calendar.getInstance().apply {
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DATE, calendar.get(Calendar.DATE))
            })
            showTimePicker()
        }
        datePicker.show(childFragmentManager, "datePicker")
    }
}