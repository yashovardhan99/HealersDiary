package com.yashovardhan99.healersdiary.database

import androidx.room.Entity
import androidx.room.Ignore
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
        val created: Date,
        @Ignore val healingsToday: Int = 0,
        @Ignore val healingsThisMonth: Int = 0
) {
    constructor(
            id: Long,
            name: String,
            charge: Long,
            due: Long,
            notes: String,
            lastModified: Date,
            created: Date,
    ) : this(id, name, charge, due, notes, lastModified, created, 0, 0)

    companion object {
        val MissingPatient = Patient(-1, "Error", 0, 0, "", Date(0), Date(0))
    }
}
