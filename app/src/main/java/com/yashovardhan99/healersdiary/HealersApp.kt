package com.yashovardhan99.healersdiary

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.play.core.splitcompat.SplitCompatApplication
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltAndroidApp
class HealersApp : SplitCompatApplication(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Timber.wtf(e, "Uncaught Exception received in Thread $t")
            exitProcess(1)
        }
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
