package com.yashovardhan99.healersdiary.utils

import android.content.Context
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.database.Patient
import java.util.*

data class Activity(
        val time: Date,
        val type: Type,
        val amount: Long,
        val patient: Patient
) {
    sealed class Type(val description: String, val icon: Icon) {
        class HEALING(context: Context) : Type("New Healing", context.getIcon(R.drawable.check))
        class PAYMENT(context: Context) : Type("Payment Received", context.getIcon(R.drawable.card))
    }
}
