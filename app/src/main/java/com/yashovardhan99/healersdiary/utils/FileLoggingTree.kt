package com.yashovardhan99.healersdiary.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder


private const val LogFilePrefix = "HealersDiary"
private const val LogFileSuffix = ".log"

@Suppress("BlockingMethodInNonBlockingContext")
class FileLoggingTree(appContext: Context) : Timber.Tree() {
    private val scope = MainScope() + Dispatchers.IO
    private var logFileName = LogFilePrefix + LogFileSuffix
    private val cacheDir = appContext.cacheDir
    private val dateTimeFormatter = DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral(" ")
        .append(DateTimeFormatter.ISO_LOCAL_TIME)
        .toFormatter()

    private fun createLogFile(): File? {
        return try {
            File.createTempFile(LogFilePrefix, LogFileSuffix, cacheDir).also {
                logFileName = it.name
            }
        } catch (e: IOException) {
            e.handle()
            null
        }
    }

    private fun IOException.handle() {
        Timber.uproot(this@FileLoggingTree)
        Timber.tag("FileLoggingTree").e(this, "IOException creating/writing to log file")
    }

    init {
        scope.launch {
            // Check all cache log files and only keep the 10 latest files
            getLogFiles(cacheDir).drop(10).forEach { file -> file.delete() }
            createLogFile()
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val timestamp = LocalDateTime.now().format(dateTimeFormatter)
        val logMessage = buildString {
            append(priority)
            append(":")
            append(" {$timestamp} ")
            appendLine(message.lines().size) // append no. of lines that are being added
            if (!tag.isNullOrBlank()) {
                append("[$tag] -> ")
            }
            appendLine(message)
        }
        scope.launch {
            var file = File(cacheDir, logFileName)
            if (!file.exists()) file = createLogFile() ?: return@launch
            try {
                file.appendText(logMessage)
            } catch (e: IOException) {
                e.handle()
            }
        }
    }

    companion object {
        fun getLogFiles(cacheDir: File): List<File> {
            return cacheDir.listFiles { _, name ->
                name.startsWith(LogFilePrefix) && name.endsWith(
                    LogFileSuffix
                )
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
        }
    }
}
