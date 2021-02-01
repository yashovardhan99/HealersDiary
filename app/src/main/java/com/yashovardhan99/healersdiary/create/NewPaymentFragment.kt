package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentNewPaymentBinding
import com.yashovardhan99.healersdiary.utils.DatePickerFragment
import com.yashovardhan99.healersdiary.utils.Icons
import com.yashovardhan99.healersdiary.utils.TimePickerFragment
import com.yashovardhan99.healersdiary.utils.buildHeader
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat

@AndroidEntryPoint
class NewPaymentFragment : Fragment() {
    private val args: NewPaymentFragmentArgs by navArgs()
    private val viewModel: CreateActivityViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentNewPaymentBinding.inflate(inflater, container, false)
        binding.name.text = args.patientName
        binding.amountBox.prefixText = NumberFormat.getCurrencyInstance().currency?.symbol ?: ""
        val dueString = NumberFormat.getCurrencyInstance().format(BigDecimal(args.currentDue).movePointLeft(2))
        binding.due.text = resources.getString(R.string.due_amount, dueString)
        binding.amountEdit.setOnFocusChangeListener { _, hasFocus ->
            val inputText = binding.amountEdit.text.toString()
            try {
                if (!hasFocus && inputText.isNotBlank()) {
                    val edited = BigDecimal(inputText).setScale(2, RoundingMode.HALF_EVEN).toPlainString()
                    binding.amountEdit.setText(edited)
                } else if (inputText.isNotBlank() && BigDecimal(inputText) == BigDecimal.ZERO) {
                    binding.amountEdit.selectAll()
                }
            } catch (e: Exception) {
                Timber.w(e, "Invalid input")
                binding.amountEdit.text?.clear()
            }
        }
        viewModel.activityCalendar.asLiveData().observe(viewLifecycleOwner) { calendar ->
            binding.dateEdit.setText(SimpleDateFormat.getDateInstance().format(calendar.time))
            binding.timeEdit.setText(SimpleDateFormat.getTimeInstance().format(calendar.time))
        }
        binding.header = context?.run {
            buildHeader(Icons.Back, R.string.new_payment, Icons.Save)
        }
        binding.heading.icon.setOnClickListener { findNavController().navigateUp() }

        val datePicker = DatePickerFragment { calendar ->
            viewModel.setActivityCalendar(calendar)
            showTimePicker()
        }
        binding.dateEdit.setOnClickListener {
            datePicker.arguments = bundleOf(Pair(DatePickerFragment.DateKey,
                    viewModel.activityCalendar.value.timeInMillis))
            datePicker.show(parentFragmentManager, "datePicker")
        }
        binding.heading.optionsIcon.setOnClickListener { save(binding) }
        binding.newPayment.setOnClickListener { save(binding) }
        binding.timeEdit.setOnClickListener { showTimePicker() }
        viewModel.error.asLiveData().observe(viewLifecycleOwner) { error ->
            if (error) {
                Snackbar.make(binding.newPayment, resources.getString(R.string.error_creating_activity), Snackbar.LENGTH_LONG)
                viewModel.resetError()
            }
        }
        return binding.root
    }

    private fun save(binding: FragmentNewPaymentBinding) {
        val amount = binding.amountEdit.text.toString()
        val notes = binding.notesEdit.text.toString()
        viewModel.createPayment(amount, notes, args.patientId)
    }

    private fun showTimePicker() {
        val timePicker = TimePickerFragment { calendar ->
            viewModel.setActivityCalendar(calendar)
        }
        timePicker.arguments = bundleOf(Pair(TimePickerFragment.TimeKey,
                viewModel.activityCalendar.value.timeInMillis))
        timePicker.show(parentFragmentManager, "timePicker")
    }
}