package com.yashovardhan99.core.database

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.room.*
import java.util.*

@TypeConverters(DateConverter::class)
@Entity(tableName = "payments", indices = [Index("time"), Index("patient_id")])
data class Payment(
        @PrimaryKey(autoGenerate = true) val id: Long,
        val time: Date,
        val amount: Long,
        val notes: String,
        @ColumnInfo(name = "patient_id") val patientId: Long
) {
    fun toBundle() = bundleOf(
            "id" to id,
            "time" to time.time,
            "amount" to amount,
            "notes" to notes,
            "pid" to patientId,
    )
}

fun Bundle.toPayment(): Payment {
    val date = getLong("time", -1).let {
        if (it == -1L) Date()
        else Date(it)
    }
    return Payment(
            getLong("id"),
            date,
            getLong("amount"),
            getString("notes", ""),
            getLong("pid"),
    )
}

data class PatientWithPayments(
        @Embedded val patient: Patient,
        @Relation(
                parentColumn = "id",
                entityColumn = "patient_id"
        )
        val payments: List<Payment>
)