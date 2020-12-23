package com.yashovardhan99.healersdiary.database

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import java.util.*

@DatabaseView("SELECT id, time, charge AS amount, notes, patient_id, 'healing' as type " +
        "FROM healings " +
        "UNION " +
        "SELECT id, time, amount, notes, patient_id, 'payment' as type " +
        "FROM payments")
data class Activity(
        val id: Long,
        val time: Date,
        val amount: Long,
        val notes: String,
        @ColumnInfo(name = "patient_id") val patientId: Long,
        val type: ActivityType
)

enum class ActivityType(val type: String) {
    HEALING("healing"),
    PAYMENT("payment")
}