package com.yashovardhan99.healersdiary.onboarding

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OnboardingViewModel @ViewModelInject constructor(@ApplicationContext application: Context) : ViewModel() {
    private val dataStore = application.createDataStore("Onboarding")

    data class OnboardingPreferences(val onboardingComplete: Boolean)
    private object PreferencesKey {
        val onboardingComplete = preferencesKey<Boolean>("onboarding_completed")
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