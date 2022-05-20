package com.yashovardhan99.healersdiary

import android.content.Intent
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.yashovardhan99.healersdiary.utils.ErrorReportingActivity
import com.yashovardhan99.healersdiary.utils.FileLoggingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltAndroidApp
class HealersApp : SplitCompatApplication(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(FileLoggingTree(applicationContext))
        }
        if (Timber.forest().any { it is FileLoggingTree }) {
            Thread.setDefaultUncaughtExceptionHandler { t, e ->
                Timber.tag("HealersApp").wtf(e, "Uncaught Exception received in Thread $t")

                startActivity(Intent(this, ErrorReportingActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                exitProcess(1)
            }
        }
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
