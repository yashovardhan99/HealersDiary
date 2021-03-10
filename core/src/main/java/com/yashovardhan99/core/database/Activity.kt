package com.yashovardhan99.core.database

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import java.time.LocalDateTime

@DatabaseView(
    "SELECT id, time, charge AS amount, notes, patient_id, 'healing' as type " +
        "FROM healings " +
        "UNION " +
        "SELECT id, time, amount, notes, patient_id, 'payment' as type " +
        "FROM payments " +
        "UNION " +
        "SELECT id, created as time, due as amount, name as notes, " +
        "id as patient_id, 'patient' AS type " +
        "FROM patients " +
        "ORDER BY time DESC"
)
data class Activity(
    val id: Long,
    val time: LocalDateTime,
    val amount: Long,
    @ColumnInfo(name = "notes") val notesOrName: String,
    @ColumnInfo(name = "patient_id") val patientId: Long,
    val type: ActivityType
)

enum class ActivityType(val type: String) {
    HEALING("healing"),
    PAYMENT("payment"),
    PATIENT("patient")
}
