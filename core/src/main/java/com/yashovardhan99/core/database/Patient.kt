package com.yashovardhan99.core.database

import androidx.room.*
import java.util.*

@TypeConverters(DateConverter::class)
@Entity(tableName = "patients")
data class Patient(
        @PrimaryKey(autoGenerate = true) val id: Long,
        val name: String,
        val charge: Long,
        val due: Long,
        val notes: String,
        @ColumnInfo(name = "last_modified") val lastModified: Date,
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
