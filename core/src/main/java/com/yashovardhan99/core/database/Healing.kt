package com.yashovardhan99.core.database

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.room.*
import com.yashovardhan99.core.toEpochMilli
import com.yashovardhan99.core.toLocalDateTime
import java.time.Instant
import java.time.LocalDateTime

@TypeConverters(DateConverter::class)
@Entity(tableName = "healings", indices = [Index("time"), Index("patient_id")])
data class Healing(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val time: LocalDateTime,
    val charge: Long,
    val notes: String,
    @ColumnInfo(name = "patient_id") val patientId: Long
) {
    fun toBundle() = bundleOf(
        "id" to id,
        "time" to time.toEpochMilli(),
        "charge" to charge,
        "notes" to notes,
        "pid" to patientId
    )
}

fun Bundle.toHealing(): Healing {
    val dateTime = getLong("time", -1).let {
        if (it == -1L) LocalDateTime.now()
        else Instant.ofEpochMilli(it).toLocalDateTime()
    }
    return Healing(
        getLong("id"),
        dateTime,
        getLong("charge"),
        getString("notes", ""),
        getLong("pid")
    )
}

data class PatientWithHealings(
    @Embedded val patient: Patient,
    @Relation(
        parentColumn = "id",
        entityColumn = "patient_id"
    )
    val healings: List<Healing>
)