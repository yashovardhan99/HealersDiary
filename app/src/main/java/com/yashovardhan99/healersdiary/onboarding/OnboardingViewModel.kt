package com.yashovardhan99.healersdiary.onboarding

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(@ApplicationContext application: Context) : ViewModel() {
    private val dataStore = application.createDataStore("Onboarding")

    data class OnboardingPreferences(val onboardingComplete: Boolean)
    private object PreferencesKey {
        val onboardingComplete = booleanPreferencesKey("onboarding_completed")
    }

    val onboardingFlow: Flow<OnboardingPreferences> = dataStore.data.map { preferences ->
        val onboardingCompleted = preferences[PreferencesKey.onboardingComplete] ?: false
        OnboardingPreferences(onboardingCompleted)
    }

    fun getStarted() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKey.onboardingComplete] = true
            }
        }
    }
}