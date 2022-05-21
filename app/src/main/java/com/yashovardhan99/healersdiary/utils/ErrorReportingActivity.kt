package com.yashovardhan99.healersdiary.utils

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.yashovardhan99.emailer.EmailAttachments
import com.yashovardhan99.emailer.sendEmail
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.ActivityErrorReportingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class ErrorReportingActivity : AppCompatActivity() {
    private fun prepareFiles(): List<Uri> {
        val logFiles = FileLoggingTree.getLogFiles(applicationContext.cacheDir)
        return logFiles.mapNotNull {
            try {
                FileProvider.getUriForFile(
                    this,
                    "com.yashovardhan99.healersdiary.fileprovider",
                    it
                )
            } catch (e: IllegalArgumentException) {
                Timber.tag("ErrorReportingActivity")
                    .e(e, "File provider authority can't access file")
                null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityErrorReportingBinding>(
            this,
            R.layout.activity_error_reporting
        )
        binding.sendReport.isEnabled = false
        lifecycleScope.launch(Dispatchers.IO) {
            val logs = prepareFiles()
            withContext(Dispatchers.Main) {
                binding.sendReport.isEnabled = true
                binding.sendReport.setOnClickListener { sendReport(logs) }
            }
        }
        binding.exit.setOnClickListener { finish() }

    }

    private fun sendReport(logs: List<Uri>) {
        val timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        sendEmail {
            chooserTitle = "Send bug report"
            receipients = listOf("teamhealersdiary@gmail.com")
            subject = "Healers Diary Crash Report : $timestamp"
            // TODO - Add more details to the report; Extract to string resources and StringBuilder functions.
            emailMessage = "Attached are the logs describing the crash"
            attachments = EmailAttachments(logs, listOf("text/plain"), "log files")
            mimeType = "text/plain"
            grantUriPermission = true
        }
    }
}