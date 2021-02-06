package com.yashovardhan99.healersdiary.online.importFirebase

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.firebase.auth.FirebaseUser
import com.yashovardhan99.healersdiary.AppDataStore
import com.yashovardhan99.healersdiary.onboarding.OnboardingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ImportFirebaseViewModel @Inject constructor(
        context: Context,
        @AppDataStore private val dataStore: DataStore<Preferences>) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user
    private val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
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
            dataStore.edit { preferences ->
                preferences[OnboardingViewModel.Companion.PreferencesKey.importComplete] = true
            }
        }
    }

    fun setUser(currentUser: FirebaseUser) {
        Timber.d("Found user = $currentUser")
        viewModelScope.launch {
            _user.emit(currentUser)
        }
    }

    fun startImport() {
        workManager.enqueueUniqueWork(
                "importFirebase",
                ExistingWorkPolicy.KEEP,
                importWorkRequest)
    }
}