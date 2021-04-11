package com.yashovardhan99.core.backup_restore

import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment

object ExportUtils {
    fun getCsvRow(vararg fields: Any): String =
        fields.joinToString(",") { it.toString().escapeString() }

    private fun String.escapeString(): String {
        return if (contains("\n") || contains("\"") || contains(",")) {
            "\"" + replace("\"", "\"\"") + "\""
        } else this
    }

    fun getExpectedSize(type: ExportWorker.Companion.DataType) = when (type) {
        ExportWorker.Companion.DataType.Healings -> 5
        ExportWorker.Companion.DataType.Patients -> 7
        ExportWorker.Companion.DataType.Payments -> 5
    }

    fun getHeaders(type: ExportWorker.Companion.DataType): List<String> {
        return when (type) {
            ExportWorker.Companion.DataType.Healings -> {
                listOf(
                    Healing::id.name, Healing::time.name,
                    Healing::charge.name, Healing::notes.name,
                    Healing::patientId.name
                )
            }
            ExportWorker.Companion.DataType.Patients -> {
                listOf(
                    Patient::id.name, Patient::name.name, Patient::charge.name,
                    Patient::due.name, Patient::notes.name,
                    Patient::lastModified.name, Patient::created.name
                )
            }
            ExportWorker.Companion.DataType.Payments -> {
                listOf(
                    Payment::id.name, Payment::time.name,
                    Payment::amount.name, Payment::notes.name,
                    Payment::patientId.name
                )
            }
        }
    }
}
