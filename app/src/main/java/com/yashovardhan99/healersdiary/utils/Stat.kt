package com.yashovardhan99.healersdiary.utils

import android.content.Context
import androidx.annotation.StringRes
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.database.ActivityType
import java.math.BigDecimal
import java.text.NumberFormat

data class Stat(val icon: Icon,
                val figure: String,
                val type: ActivityType,
                @StringRes val description: Int) {

    companion object {
        fun Context.healingsToday(healings: Int) = Stat(getIcon(R.drawable.trend), healings.toString(), ActivityType.HEALING, R.string.healings_today)
        fun Context.healingsThisMonth(healings: Int) = Stat(getIcon(R.drawable.calendar), healings.toString(), ActivityType.HEALING, R.string.healings_this_month)
        fun Context.healingsLastMonth(healings: Int) = Stat(getIcon(R.drawable.archive), healings.toString(), ActivityType.HEALING, R.string.healings_last_month)
        fun Context.paymentDue(due: BigDecimal) = Stat(getIcon(R.drawable.square_check), NumberFormat.getCurrencyInstance().format(due), ActivityType.PAYMENT, R.string.payment_due)
        fun Context.earnedThisMonth(amount: Double) = Stat(getIcon(R.drawable.clock), NumberFormat.getCurrencyInstance().format(amount), ActivityType.HEALING, R.string.earned_this_month)
        fun Context.earnedLastMonth(amount: Double) = Stat(getIcon(R.drawable.cart), NumberFormat.getCurrencyInstance().format(amount), ActivityType.HEALING, R.string.earned_last_month)
    }
}