package com.yashovardhan99.core.backup_restore

import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import kotlin.math.roundToInt

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

    fun getCurrentProgress(
        currentMask: Int,
        selectedMask: Int,
        done: IntArray = IntArray(3),
        total: IntArray = IntArray(3)
    ): Int {
        var curProgress = 0
        if (currentMask > DataType.Patients.mask && DataType.Patients in selectedMask)
            curProgress += DataType.Patients.maxProgress
        else if (currentMask == DataType.Patients.mask) curProgress +=
            getTypeProgress(DataType.Patients, done[0], total[0])
        if (currentMask > DataType.Healings.mask && DataType.Healings in selectedMask)
            curProgress += DataType.Healings.maxProgress
        else if (currentMask == DataType.Healings.mask) curProgress +=
            getTypeProgress(DataType.Healings, done[1], total[1])
        if (currentMask > DataType.Payments.mask && DataType.Payments in selectedMask)
            curProgress += DataType.Payments.maxProgress
        else if (currentMask == DataType.Payments.mask) curProgress +=
            getTypeProgress(DataType.Payments, done[2], total[2])
        return curProgress
    }

    fun getMaxProgress(
        selectedMask: Int,
    ): Int {
        var max = 0
        if (DataType.Patients in selectedMask) max += DataType.Patients.maxProgress
        if (DataType.Healings in selectedMask) max += DataType.Healings.maxProgress
        if (DataType.Payments in selectedMask) max += DataType.Payments.maxProgress
        return max
    }

    private fun getTypeProgress(
        type: DataType,
        done: Int,
        total: Int
    ): Int {
        return if (total == 0) 0
        else (done.toFloat() / total * type.maxProgress).roundToInt()
    }

    object Input {
        const val PATIENTS_FILE_URI_KEY = "patient_file_uri"
        const val HEALINGS_FILE_URI_KEY = "healings_file_uri"
        const val PAYMENTS_FILE_URI_KEY = "payments_file_uri"
        const val DATA_TYPE_KEY = "data_type"
        const val ExportFolderUriKey = "export_folder_uri"
    }

    // Progress constants for sending updates and results
    object Progress {
        const val ProgressMessage = "progress_message" // Message being shown in notification
        const val RequiredBit = "required_bit" // The data type(s) bit required to be handled
        const val CurrentBit = "current_bit" // The data type bit being currently being handled
        const val Timestamp = "timestamp" // Time in ms of the last update

        // IntArray will always be in order [Patients, Healings, Payments]
        const val ExportCounts = "export_count" // IntArray containing the no. of exports done
        const val ExportTotal = "export_total" // IntArray containing the total no. to export
        const val FileErrorBit = "file_error_bit" // Bit mask of where the file error occurred

        const val ImportSuccess = "import_success" // IntArray containing the no. of successes
        const val ImportFailure = "import_failure" // IntArray containing the no. of failures
        const val InvalidFormatBit = "invalid_format_bit" // Bit of where invalid format was found
    }

    sealed class DataType(val mask: Int, val idx: Int, val maxProgress: Int) {
        object Patients : DataType(1 shl 0, 0, 10)
        object Healings : DataType(1 shl 1, 1, 200)
        object Payments : DataType(1 shl 2, 2, 40)
        companion object {
            const val DoneMask = 7
        }
    }

    /**
     * Check whether a [DataType] is included in a bit-mask.
     * @param type Data type to check
     * @return true if the data type is included in the given mask
     */
    operator fun Int.contains(type: DataType): Boolean {
        return this and type.mask > 0
    }

    /**
     * Include the data type in the bit.
     */
    operator fun Int.plus(type: DataType): Int {
        return this or type.mask
    }

    /**
     * Remove the data type from the bit, if present.
     */
    operator fun Int.minus(type: DataType): Int {
        return if (type in this) this xor type.mask
        else this
    }
}
