package com.yashovardhan99.healersdiary.database

import androidx.room.*
import java.util.*

@TypeConverters(DateConverter::class)
@Entity(tableName = "healings")
data class Healing(
        @PrimaryKey(autoGenerate = true) val id: Long,
        var time: Date,
        var charge: Long,
        var notes: String,
        @ColumnInfo(name = "patient_id") val patientId: Long
)

data class PatientWithHealings(
        @Embedded val patient: Patient,
        @Relation(
                parentColumn = "id",
                entityColumn = "patient_id"
        )
        val healings: List<Healing>
)