package com.yashovardhan99.core.utils

import androidx.annotation.StringRes
import com.yashovardhan99.core.R
import com.yashovardhan99.core.database.Patient
import java.util.Date

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
            object PATIENT : Type(R.string.patient_added, Icons.CustomStatic(R.drawable.add_person))
        }

        companion object {
            fun getSeparator(
                before: Activity?,
                after: Activity?
            ) = when {
                before == null && after != null -> ActivitySeparator(
                    Utils.getHeading(after.time)
                )
                before != null && after != null &&
                    Utils.getHeading(before.time) != Utils.getHeading(after.time) ->
                    ActivitySeparator(Utils.getHeading(after.time))
                else -> null
            }
        }
    }
}
