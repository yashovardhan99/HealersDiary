package com.yashovardhan99.healersdiary.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashovardhan99.healersdiary.AppDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(@ApplicationContext application: Context,
                                              @AppDataStore private val dataStore: DataStore<Preferences>) : ViewModel() {

    companion object {
        data class OnboardingPreferences(val onboardingComplete: Boolean,
                                         val importComplete: Boolean)

        object PreferencesKey {
            val onboardingComplete = booleanPreferencesKey("onboarding_completed")
            val importComplete = booleanPreferencesKey("onboarding_import_completed")
        }
    }

    private val _onboardingPrefs = MutableLiveData<OnboardingPreferences>()
    val onboardingPrefs: LiveData<OnboardingPreferences> = _onboardingPrefs

    init {
        Timber.d(application.packageName)
        viewModelScope.launch {
            dataStore.data.onEach { Timber.d("Pref = $it") }.collect { preferences ->
                val onboardingCompleted = preferences[PreferencesKey.onboardingComplete]
                        ?: false
                val importCompleted = preferences[PreferencesKey.importComplete]
                        ?: false
                _onboardingPrefs.value = OnboardingPreferences(onboardingCompleted, importCompleted)
            }
        }
    }

    fun getStarted() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKey.onboardingComplete] = true
            }
        }
    }
}
