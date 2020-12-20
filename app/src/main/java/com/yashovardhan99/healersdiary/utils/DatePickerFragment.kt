package com.yashovardhan99.healersdiary.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment(val onDateSet: (Calendar) -> Unit) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private val calendar = Calendar.getInstance()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.run {
            calendar.timeInMillis = getLong(DateKey)
        }
        val datePickerDialog = DatePickerDialog(requireContext(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val cur = Calendar.getInstance()
        if (calendar > cur) onDateSet(cur)
        else onDateSet(calendar)
    }

    companion object {
        const val DateKey = "DATE"
    }
}