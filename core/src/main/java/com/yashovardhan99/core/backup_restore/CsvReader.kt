package com.yashovardhan99.core.backup_restore

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CsvReader(private val bufferedReader: BufferedReader) {
    constructor(inputStream: InputStream) : this(inputStream.bufferedReader())

    private fun String.unescapeString(): String {
        return if (startsWith("\"") && endsWith("\"")) {
            substringAfter("\"")
                .substringBeforeLast("\"")
                .replace("\"\"", "\"")
        } else this
    }

    private enum class State {
        Field,
        Delimiter,
        QuotedField,
        InsideQuote,
    }

    /**
     * Parse a single row from the CSV
     * If we have reached EOF, returns an empty list
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun parseRow(): List<String> {
        var curState = State.Delimiter
        val curRow = mutableListOf<String>()
        val curEntry = StringBuilder()
        while (true) {
            val parsingLine = withContext(Dispatchers.IO) {
                try {
                    bufferedReader.readLine()
                } catch (e: IOException) {
                    e.printStackTrace()
                    ""
                }
            } ?: break
            parsingLine.forEach { c ->
                when {
                    // comma outside quotes OR just after a quote (End of quote block)
                    c == ',' && (
                        curState == State.Field ||
                            curState == State.Delimiter ||
                            curState == State.InsideQuote
                        )
                    -> {

                        curRow.add(curEntry.toString())
                        curEntry.clear()
                        curState = State.Delimiter
                    }
                    // Encountered quote just after comma (starting quote)
                    c == '"' && curState == State.Delimiter -> {
                        curState = State.QuotedField
                    }
                    // Encountered quotes inside quotes (can either mean end of quotes OR an escaped quote
                    c == '"' && curState == State.QuotedField -> {
                        curState = State.InsideQuote
                    }
                    // Encountered 2 quotes inside a quoted field (escaped quote)
                    c == '"' && curState == State.InsideQuote -> {
                        curEntry.append('"')
                        curState = State.QuotedField
                    }
                    // Any other character inside quoted field is added AS IT IS
                    curState == State.QuotedField -> {
                        curEntry.append(c)
                    }
                    // Any character after quote (but not a comma)
                    // Invalid
                    curState == State.InsideQuote -> {
                        return emptyList()
                    }
                    // Unquoted characters
                    else -> {
                        curEntry.append(c)
                        curState = State.Field
                    }
                }
            }
            // If inside a quoted field -> Continue to next line (also inside quotes)
            if (curState != State.QuotedField) {
                curRow.add(curEntry.toString())
                break
            }
        }
        return curRow.map { it.unescapeString() }
    }

    suspend fun close() {
        withContext(Dispatchers.IO) {
            @Suppress("BlockingMethodInNonBlockingContext")
            bufferedReader.close()
        }
    }
}
