package com.yashovardhan99.healersdiary.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@TypeConverters(DateConverter::class)
@Entity(tableName = "patients")
data class Patient(
        @PrimaryKey(autoGenerate = true) val id: Long,
        var name: String,
        var charge: Long,
        var due: Long,
        var notes: String,
        var lastModified: Date,
        val created: Date
) {
    companion object {
        val MissingPatient = Patient(-1, "Error", 0, 0, "", Date(0), Date(0))
    }
}
