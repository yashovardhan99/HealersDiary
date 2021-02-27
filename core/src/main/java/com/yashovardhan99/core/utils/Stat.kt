package com.yashovardhan99.core.utils

import androidx.annotation.StringRes
import com.yashovardhan99.core.R
import com.yashovardhan99.core.database.ActivityType
import java.math.BigDecimal
import java.text.NumberFormat

data class Stat(val icon: Icons,
                val figure: String,
                val type: ActivityType,
                @StringRes val description: Int) {

    companion object {
        fun healingsToday(healings: Int) = Stat(Icons.CustomStatic(R.drawable.trend), healings.toString(), ActivityType.HEALING, R.string.healings_today)
        fun healingsThisMonth(healings: Int) = Stat(Icons.CustomStatic(R.drawable.calendar), healings.toString(), ActivityType.HEALING, R.string.healings_this_month)
        fun healingsLastMonth(healings: Int) = Stat(Icons.CustomStatic(R.drawable.archive), healings.toString(), ActivityType.HEALING, R.string.healings_last_month)
        fun paymentDue(due: BigDecimal) = Stat(Icons.CustomStatic(R.drawable.square_check), NumberFormat.getCurrencyInstance().format(due), ActivityType.PAYMENT, R.string.payment_due)
        fun earnedThisMonth(amount: Double) = Stat(Icons.CustomStatic(R.drawable.clock), NumberFormat.getCurrencyInstance().format(amount), ActivityType.HEALING, R.string.earned_this_month)
        fun earnedLastMonth(amount: Double) = Stat(Icons.CustomStatic(R.drawable.cart), NumberFormat.getCurrencyInstance().format(amount), ActivityType.HEALING, R.string.earned_last_month)
    }
}