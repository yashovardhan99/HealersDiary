package com.yashovardhan99.healersdiary.utils

import androidx.annotation.StringRes
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.core.database.Patient
import java.util.*

sealed class ActivityParent {
    data class ActivitySeparator(val heading: String) : ActivityParent()
    data class Activity(
            val id: Long,
            val time: Date,
            val type: Type,
            val amount: Long,
            val patient: Patient
    ) : ActivityParent() {
        sealed class Type(@StringRes val description: Int, val icon: Icons) {
            object HEALING : Type(R.string.new_healing, Icons.CustomStatic(R.drawable.check))
            object PAYMENT : Type(R.string.payment_received, Icons.CustomStatic(R.drawable.card))
        }
    }
}
