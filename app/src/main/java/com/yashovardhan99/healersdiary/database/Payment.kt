package com.yashovardhan99.healersdiary.database

import androidx.room.*
import java.util.*

@TypeConverters(DateConverter::class)
@Entity(tableName = "payments")
data class Payment(
        @PrimaryKey(autoGenerate = true) val id: Long,
        val time: Date,
        val amount: Long,
        val notes: String,
        @ColumnInfo(name = "patient_id") val patientId: Long
)

data class PatientWithPayments(
        @Embedded val patient: Patient,
        @Relation(
                parentColumn = "id",
                entityColumn = "patient_id"
        )
        val payments: List<Payment>
)