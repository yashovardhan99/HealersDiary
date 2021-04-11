package com.yashovardhan99.core.backup_restore

object ExportUtils {
    fun getCsvRow(vararg fields: Any): String =
        fields.joinToString(",") { it.toString().escapeString() }

    private fun String.escapeString(): String {
        return if (contains("\n") || contains("\"") || contains(",")) {
            "\"" + replace("\"", "\"\"") + "\""
        } else this
    }
}
