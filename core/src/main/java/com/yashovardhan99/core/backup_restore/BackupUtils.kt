package com.yashovardhan99.core.backup_restore

import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment

object BackupUtils {
    fun getCsvRow(vararg fields: Any): String =
        fields.joinToString(",") { it.toString().escapeString() }

    private fun String.escapeString(): String {
        return if (contains("\n") || contains("\"") || contains(",")) {
            "\"" + replace("\"", "\"\"") + "\""
        } else this
    }

    fun getExpectedSize(type: DataType) = when (type) {
        DataType.Healings -> 5
        DataType.Patients -> 7
        DataType.Payments -> 5
    }

    fun getHeaders(type: DataType): List<String> {
        return when (type) {
            DataType.Healings -> {
                listOf(
                    Healing::id.name, Healing::time.name,
                    Healing::charge.name, Healing::notes.name,
                    Healing::patientId.name
                )
            }
            DataType.Patients -> {
                listOf(
                    Patient::id.name, Patient::name.name, Patient::charge.name,
                    Patient::due.name, Patient::notes.name,
                    Patient::lastModified.name, Patient::created.name
                )
            }
            DataType.Payments -> {
                listOf(
                    Payment::id.name, Payment::time.name,
                    Payment::amount.name, Payment::notes.name,
                    Payment::patientId.name
                )
            }
        }
    }

    // Progress constants for sending updates and results
    object Progress {
        const val ProgressPercent = "progress_percent" // Progress in 100
        const val ProgressMessage = "progress_message" // Message being shown in notification
        const val RequiredBit = "required_bit" // The data type(s) bit required to be handled
        const val CurrentBit = "current_bit" // The data type bit being currently being handled
        const val DoneBit = "done_bit" // The data type(s) bit which have been handled

        // IntArray will always be in order [Patients, Healings, Payments]
        const val ExportCounts = "export_count" // IntArray containing the no. of exports done
        const val ExportTotal = "export_total" // IntArray containing the total no. to export
        const val FileErrorBit = "file_error_bit" // Bit mask of where the file error occurred

        const val ImportSuccess = "import_success" // IntArray containing the no. of successes
        const val ImportFailure = "import_failure" // IntArray containing the no. of failures
        const val InvalidFormatBit = "invalid_format_bit" // Bit of where invalid format was found
    }

    sealed class DataType(val mask: Int) {
        object Patients : DataType(1 shl 0)
        object Healings : DataType(1 shl 1)
        object Payments : DataType(1 shl 2)
    }
}
