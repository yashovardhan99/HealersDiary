package com.yashovardhan99.healersdiary.utils

import android.text.format.DateUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.NumberFormat
import java.util.*

@BindingAdapter("bind:amountText")
fun setAmountText(view: TextView, amount: Long) {
    view.text = NumberFormat.getCurrencyInstance().format(amount.toDouble() / 100)
}

@BindingAdapter("bind:date")
fun setFormattedDate(view: TextView, date: Date) {
    view.text = DateUtils.getRelativeTimeSpanString(date.time, Date().time, DateUtils.MINUTE_IN_MILLIS)
}