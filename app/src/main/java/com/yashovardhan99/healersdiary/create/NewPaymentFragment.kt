package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.pm.ShortcutManagerCompat
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
import com.yashovardhan99.healersdiary.databinding.FragmentNewPaymentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.LocalTime

@AndroidEntryPoint
class NewPaymentFragment : Fragment() {
    private val args: NewPaymentFragmentArgs by navArgs()
    private val viewModel: CreateActivityViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPaymentBinding.inflate(inflater, container, false)
        binding.name.text = args.patientName
        binding.amountBox.prefixText = NumberFormat.getCurrencyInstance().currency?.symbol ?: ""
        val dueString =
            NumberFormat.getCurrencyInstance().format(BigDecimal(args.currentDue).movePointLeft(2))
        binding.due.text = resources.getString(R.string.due_amount, dueString)
        binding.amountEdit.setOnFocusChangeListener { _, hasFocus ->
            val inputText = binding.amountEdit.text.toString()
            try {
                if (!hasFocus && inputText.isNotBlank()) {
                    val edited =
                        BigDecimal(inputText).setScale(2, RoundingMode.HALF_EVEN).toPlainString()
                    binding.amountEdit.setText(edited)
                } else if (inputText.isNotBlank() && BigDecimal(inputText) == BigDecimal.ZERO) {
                    binding.amountEdit.selectAll()
                }
            } catch (e: Exception) {
                Timber.w(e, "Invalid input")
                binding.amountEdit.text?.clear()
            }
        }
        viewModel.activityTime.asLiveData().observe(viewLifecycleOwner) { time ->
            binding.dateEdit.setText(time.formatDate())
            binding.timeEdit.setText(time.formatTime())
        }
        binding.header = context?.run {
            buildHeader(
                Icons.Back,
                if (viewModel.getPayment().value != null) R.string.edit_payment else R.string.new_payment,
                Icons.Save
            )
        }
        binding.heading.icon.setOnClickListener {
            if (!findNavController().popBackStack()) activity?.finish()
        }

        binding.heading.optionsIcon.setOnClickListener { save(binding) }
        binding.newPayment.setOnClickListener { save(binding) }
        binding.dateEdit.setOnClickListener { showDatePicker() }
        binding.timeEdit.setOnClickListener { showTimePicker() }
        viewModel.error.asLiveData().observe(viewLifecycleOwner) { error ->
            if (error) {
                Snackbar.make(
                    binding.newPayment,
                    resources.getString(R.string.error_creating_activity),
                    Snackbar.LENGTH_LONG
                )
                viewModel.resetError()
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.getPayment().collect { payment ->
                if (payment != null) {
                    val edited = BigDecimal(payment.amount).movePointLeft(2)
                        .setScale(2, RoundingMode.HALF_EVEN).toPlainString()
                    binding.amountEdit.setText(edited)
                    binding.notesEdit.setText(payment.notes)
                    binding.newPayment.setText(R.string.update)
                    binding.header =
                        context?.run { buildHeader(Icons.Back, R.string.edit_payment, Icons.Save) }
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.CreatePayment.trackView()
    }

    private fun save(binding: FragmentNewPaymentBinding) {
        val amount = binding.amountEdit.text.toString()
        val notes = binding.notesEdit.text.toString()
        context?.let {
            ShortcutManagerCompat.reportShortcutUsed(it, "newPayment")
        }
        viewModel.createPayment(amount, notes, args.patientId)
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