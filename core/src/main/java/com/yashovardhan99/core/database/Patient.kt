package com.yashovardhan99.core.database

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.yashovardhan99.core.getLocalDateTimeFromMillis
import com.yashovardhan99.core.toEpochMilli
import java.time.LocalDateTime

@TypeConverters(DateConverter::class)
@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val charge: Long,
    val due: Long,
    val notes: String,
    @ColumnInfo(name = "last_modified") val lastModified: LocalDateTime,
    val created: LocalDateTime,
    @Ignore val healingsToday: Int = 0,
    @Ignore val healingsThisMonth: Int = 0
) {
    constructor(
        id: Long,
        name: String,
        charge: Long,
        due: Long,
        notes: String,
        lastModified: LocalDateTime,
        created: LocalDateTime,
    ) : this(
        id,
        name,
        charge,
        due,
        notes,
        lastModified,
        created,
        0,
        0
    )

    fun toBundle(): Bundle {
        return bundleOf(
            "id" to id,
            "name" to name,
            "charge" to charge,
            "due" to due,
            "notes" to notes,
            "created" to created.toEpochMilli(),
            "modified" to lastModified.toEpochMilli()
        )
    }

    companion object {
        val MissingPatient = Patient(-1, "Error", 0, 0, "", LocalDateTime.MIN, LocalDateTime.MIN)
    }
}

fun Bundle.toPatient(): Patient {
    val created = getLong("created", -1).let {
        if (it == -1L) LocalDateTime.now()
        else getLocalDateTimeFromMillis(it)
    }
    val modified = getLong("modified", -1).let {
        if (it == -1L) LocalDateTime.now()
        else getLocalDateTimeFromMillis(it)
    }
    return Patient(
        getLong("id"),
        getString("name", ""),
        getLong("charge"),
        getLong("due"),
        getString("notes", ""),
        modified,
        created
    )
}