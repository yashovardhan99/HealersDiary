package com.yashovardhan99.healersdiary

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.play.core.splitcompat.SplitCompatApplication
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class HealersApp : SplitCompatApplication(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
