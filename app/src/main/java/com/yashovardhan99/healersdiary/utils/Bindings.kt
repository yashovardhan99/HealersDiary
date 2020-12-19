package com.yashovardhan99.healersdiary.utils

import android.text.format.DateUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.yashovardhan99.healersdiary.R
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

@BindingAdapter("bind:amountText")
fun setAmountText(view: TextView, amount: Long) {
    val amtInDecimal = BigDecimal(amount).movePointLeft(2)
    view.text = NumberFormat.getCurrencyInstance().format(amtInDecimal)
}

@BindingAdapter("bind:date")
fun setFormattedDate(view: TextView, date: Date) {
    view.text = DateUtils.getRelativeTimeSpanString(date.time, Date().time, DateUtils.MINUTE_IN_MILLIS)
}

@BindingAdapter("bind:healingsToday")
fun setHealingsToday(view: TextView, healings: Int) {
    val today = view.context.resources.getString(R.string.today)
    view.text = view.context.resources.getQuantityString(R.plurals.healing, healings, healings, today)
}

@BindingAdapter("bind:healingsThisMonth")
fun setHealingsThisMonth(view: TextView, healings: Int) {
    view.text = view.context.resources.getString(R.string.number_this_month_text, healings)
}