package com.yashovardhan99.core.utils

import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import com.yashovardhan99.core.R
import com.yashovardhan99.core.toEpochMilli
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime

@BindingAdapter("amountText")
fun setAmountText(view: TextView, amount: Long) {
    val amtInDecimal = BigDecimal(amount).movePointLeft(2)
    view.text = NumberFormat.getCurrencyInstance().format(amtInDecimal)
}

@BindingAdapter("dateTime")
fun setFormattedDateTime(view: TextView, dateTime: LocalDateTime?) {
    view.text = if (dateTime == null) ""
    else DateUtils.getRelativeTimeSpanString(
        dateTime.toEpochMilli(),
        Instant.now().toEpochMilli(),
        DateUtils.MINUTE_IN_MILLIS
    )
}

@BindingAdapter("healingsToday")
fun setHealingsToday(view: TextView, healings: Int) {
    val today = view.context.resources.getString(R.string.today)
    view.text =
        view.context.resources.getQuantityString(R.plurals.healing, healings, healings, today)
}

@BindingAdapter("healingsThisMonth")
fun setHealingsThisMonth(view: TextView, healings: Int) {
    view.text = view.context.resources.getString(R.string.number_this_month_text, healings)
}

@BindingAdapter("android:src")
fun setImageResource(imageView: ImageView, @DrawableRes resource: Int) {
    imageView.setImageResource(resource)
}

@BindingAdapter("android:contentDescription")
fun setContentDescriptionRes(view: View, @StringRes resource: Int?) {
    view.contentDescription = resource?.let { view.resources.getString(it) }
}
