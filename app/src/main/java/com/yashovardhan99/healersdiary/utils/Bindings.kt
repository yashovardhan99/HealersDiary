package com.yashovardhan99.healersdiary.utils

import android.text.format.DateUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.yashovardhan99.healersdiary.BuildConfig
import com.yashovardhan99.healersdiary.R
import timber.log.Timber
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

@BindingAdapter("amountText")
fun setAmountText(view: TextView, amount: Long) {
    val amtInDecimal = BigDecimal(amount).movePointLeft(2)
    view.text = NumberFormat.getCurrencyInstance().format(amtInDecimal)
}

@BindingAdapter("date")
fun setFormattedDate(view: TextView, date: Date) {
    view.text = DateUtils.getRelativeTimeSpanString(date.time, Date().time, DateUtils.MINUTE_IN_MILLIS)
}

@BindingAdapter("healingsToday")
fun setHealingsToday(view: TextView, healings: Int) {
    val today = view.context.resources.getString(R.string.today)
    view.text = view.context.resources.getQuantityString(R.plurals.healing, healings, healings, today)
}

@BindingAdapter("healingsThisMonth")
fun setHealingsThisMonth(view: TextView, healings: Int) {
    view.text = view.context.resources.getString(R.string.number_this_month_text, healings)
}

@BindingAdapter(value = ["showDebugVersion"])
fun TextView.setVersion(showDebugVersion: Boolean) {
    Timber.d("showDebug = $showDebugVersion")
    text = if (showDebugVersion && BuildConfig.DEBUG) {
        "Application id = ${BuildConfig.APPLICATION_ID}\n" +
                "Build type = ${BuildConfig.BUILD_TYPE}\n" +
                "Version code = ${BuildConfig.VERSION_CODE}\n" +
                "Version name = ${BuildConfig.VERSION_NAME}"
    } else {
        context.resources.getString(R.string.version_s, BuildConfig.VERSION_NAME)
    }
}