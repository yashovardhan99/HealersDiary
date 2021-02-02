package com.yashovardhan99.healersdiary.online

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashovardhan99.healersdiary.AppDataStore
import com.yashovardhan99.healersdiary.onboarding.OnboardingViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ImportFirebaseViewModel @Inject constructor(
        application: Context,
        @AppDataStore private val dataStore: DataStore<Preferences>) : ViewModel() {

    init {
        Timber.d(application.packageName)
    }

    fun importCompleted() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[OnboardingViewModel.Companion.PreferencesKey.onboardingComplete] = true
                Timber.d("Preferences changed $preferences $dataStore")
            }
        }
    }
}