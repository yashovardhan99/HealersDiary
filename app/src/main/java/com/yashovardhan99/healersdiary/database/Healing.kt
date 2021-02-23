package com.yashovardhan99.healersdiary.database

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.room.*
import java.util.*

@TypeConverters(DateConverter::class)
@Entity(tableName = "healings", indices = [Index("time"), Index("patient_id")])
data class Healing(
        @PrimaryKey(autoGenerate = true) val id: Long,
        val time: Date,
        val charge: Long,
        val notes: String,
        @ColumnInfo(name = "patient_id") val patientId: Long
) {
    fun toBundle() = bundleOf(
            "id" to id,
            "time" to time.time,
            "charge" to charge,
            "notes" to notes,
            "pid" to patientId)
}
fun Bundle.toHealing(): Healing {
    val date = getLong("time", -1).let {
        if (it == -1L) Date()
        else Date(it)
    }
    return Healing(
            getLong("id"),
            date,
            getLong("charge"),
            getString("notes", ""),
            getLong("pid"))
}
data class PatientWithHealings(
        @Embedded val patient: Patient,
        @Relation(
                parentColumn = "id",
                entityColumn = "patient_id"
        )
        val healings: List<Healing>
)