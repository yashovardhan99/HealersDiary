package com.yashovardhan99.healersdiary.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * Created by Yashovardhan99 on 1/11/18 as a part of HealersDiary.
 */
class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    private lateinit var listener: DatePickerListener

    init {
        val c = Calendar.getInstance()
        setDate(c)
    }

    private fun setDate(c: Calendar) {
        year = c.get(Calendar.YEAR)
        month = c.get(Calendar.MONTH)
        day = c.get(Calendar.DAY_OF_MONTH)
    }

    interface DatePickerListener {
        fun onDateSet(dialogFragment: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as DatePickerListener
        } catch (e: ClassCastException) {
            throw ClassCastException(targetFragment!!.toString() + "must implement DatePickerListener")
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (arguments != null) {
            val c = Calendar.getInstance()
            c.timeInMillis = arguments!!.getLong("DATE")
            setDate(c)
        }
        val datePickerDialog = DatePickerDialog(activity!!, this, year, month, day)
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        this.year = year
        this.month = month
        this.day = dayOfMonth
        listener.onDateSet(this)
    }
}
