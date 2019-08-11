package com.yashovardhan99.healersdiary.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * Created by Yashovardhan99 on 1/11/18 as a part of HealersDiary.
 */
class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    internal var hour: Int = 0
    internal var minute: Int = 0
    lateinit var listener: TimePickerListener

    init {
        val c = Calendar.getInstance()
        setTime(c)
    }

    private fun setTime(c: Calendar) {
        hour = c.get(Calendar.HOUR_OF_DAY)
        minute = c.get(Calendar.MINUTE)
    }

    interface TimePickerListener {
        fun onTimeSet(dialogFragment: DialogFragment)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = targetFragment as TimePickerListener
        } catch (e: ClassCastException) {
            throw ClassCastException(targetFragment!!.toString() + "must implement TimePickerListener")
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (arguments != null) {
            val c = Calendar.getInstance()
            c.timeInMillis = arguments!!.getLong("DATE")
            setTime(c)
        }
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(context))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        this.hour = hourOfDay
        this.minute = minute
        listener.onTimeSet(this)
    }
}
