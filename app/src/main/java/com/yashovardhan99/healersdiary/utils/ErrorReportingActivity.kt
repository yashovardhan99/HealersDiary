package com.yashovardhan99.healersdiary.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
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
        // TODO - Generalize send email part of this
        val timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val clipDescription = ClipDescription("log files", arrayOf("text/plain"))
        val clipItems = logs.map { ClipData.Item(it) }
        val emailFilterIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        val emailClients = packageManager.queryIntentActivities(emailFilterIntent, 0)
        val emailIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = "text/plain"
            if (clipItems.isNotEmpty()) {
                clipData = ClipData(clipDescription, clipItems.first()).apply {
                    clipItems.drop(1).forEach { addItem(it) }
                }
            }
            putExtra(Intent.EXTRA_EMAIL, arrayOf("teamhealersdiary@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Healers Diary Crash Report : $timestamp")
            putExtra(Intent.EXTRA_TEXT, "Attached are the logs describing the crash")
            // TODO - Add more details to the report; Extract to string resources and StringBuilder functions.
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(logs))
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val shareTargets = packageManager.queryIntentActivities(emailIntent, 0)
        val targetedIntents = shareTargets.filter { originalResult ->
            emailClients.any { originalResult.activityInfo.packageName == it.activityInfo.packageName }
        }.map { Intent(emailIntent).apply { `package` = it.activityInfo.packageName } }
        val finalIntent = Intent.createChooser(targetedIntents.first(), "Send bug report")
        finalIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.toTypedArray())
        startActivity(finalIntent)
    }
}