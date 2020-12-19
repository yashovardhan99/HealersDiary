package com.yashovardhan99.healersdiary.utils

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(val onTimeSet: (Calendar) -> Unit) : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private val calendar = Calendar.getInstance()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.run {
            calendar.timeInMillis = getLong(TimeKey)
        }
        val timePicker = TimePickerDialog(
                context, this, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(context))

        return timePicker
    }

    companion object {
        const val TimeKey = "TIME"
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        val cur = Calendar.getInstance()
        if (calendar > cur) calendar.timeInMillis = cur.timeInMillis
        onTimeSet(calendar)
    }
}