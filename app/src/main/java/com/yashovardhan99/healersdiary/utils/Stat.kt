package com.yashovardhan99.healersdiary.utils

import android.content.Context
import androidx.annotation.StringRes
import com.yashovardhan99.healersdiary.R
import java.text.NumberFormat

data class Stat(val icon: Icon,
                val figure: String,
                @StringRes val description: Int) {

    companion object {
        fun Context.healingsToday(healings: Int) = Stat(getIcon(R.drawable.trend), healings.toString(), R.string.healings_today)
        fun Context.healingsThisMonth(healings: Int) = Stat(getIcon(R.drawable.calendar), healings.toString(), R.string.healings_this_month)
        fun Context.earnedThisMonth(amount: Double) = Stat(getIcon(R.drawable.clock), NumberFormat.getCurrencyInstance().format(amount), R.string.earned_this_month)
        fun Context.earnedLastMonth(amount: Double) = Stat(getIcon(R.drawable.cart), NumberFormat.getCurrencyInstance().format(amount), R.string.earned_last_month)
    }
}