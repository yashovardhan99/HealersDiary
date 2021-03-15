package com.yashovardhan99.healersdiary.online.importFirebase

import android.content.Context
import androidx.lifecycle.*
import androidx.work.*
import com.google.firebase.auth.FirebaseUser
import com.yashovardhan99.core.analytics.AnalyticsEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ImportFirebaseViewModel @Inject constructor(
        context: Context) : ViewModel() {

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
    private val _workObserver = MutableLiveData<UUID>()
    val workObserver: LiveData<WorkInfo> = Transformations.switchMap(_workObserver) {
        if (it != null) workManager.getWorkInfoByIdLiveData(it)
        else null
    }

    fun setUser(currentUser: FirebaseUser) {
        Timber.d("Found user = $currentUser")
        viewModelScope.launch {
            _user.emit(currentUser)
        }
    }

    fun startImport() {
        viewModelScope.launch {
            val workInfos = workManager.getWorkInfosForUniqueWork("importFirebase").await()
            val activeWorker = workInfos.find { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            if (activeWorker != null) {
                _workObserver.value = activeWorker.id
                Timber.d("Active worker = $activeWorker")
            } else {
                AnalyticsEvent.Import.Requested.trackEvent()
                workManager.enqueueUniqueWork(
                        "importFirebase",
                        ExistingWorkPolicy.KEEP,
                        importWorkRequest)
                _workObserver.value = importWorkRequest.id
            }
        }
    }
}