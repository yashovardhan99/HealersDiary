package com.yashovardhan99.healersdiary.online.importFirebase

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.FirebaseUser
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.database.HealersDataStore
import com.yashovardhan99.core.database.OnboardingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ImportFirebaseViewModel @Inject constructor(
        context: Context,
        private val dataStore: HealersDataStore) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user
    private val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private val importWorkRequest = OneTimeWorkRequestBuilder<ImportWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS)
            .addTag("importFirebase")
            .build()
    private val workManager = WorkManager.getInstance(context)
    val workObserver = workManager.getWorkInfoByIdLiveData(importWorkRequest.id)

    fun importCompleted() {
        viewModelScope.launch {
            dataStore.updateOnboardingState(OnboardingState.OnboardingCompleted)
        }
    }

    fun setUser(currentUser: FirebaseUser) {
        Timber.d("Found user = $currentUser")
        viewModelScope.launch {
            _user.emit(currentUser)
        }
    }

    fun startImport() {
        AnalyticsEvent.Import.Requested.trackEvent()
        workManager.enqueueUniqueWork(
                "importFirebase",
                ExistingWorkPolicy.KEEP,
                importWorkRequest)
    }
}