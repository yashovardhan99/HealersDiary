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
import com.yashovardhan99.healersdiary.utils.DangerousDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Viewmodel used by Splash screen in onboarding flow
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(@ApplicationContext application: Context,
                                              @AppDataStore private val dataStore: DataStore<Preferences>,
                                              private val repository: OnboardingRepository) : ViewModel() {

    companion object {
        /**
         * Onboarding preferences for datastore.
         * @param onboardingComplete Used to indicate whether onboarding has been completed
         * @param importComplete Used to indicate import from v1 is complete
         * @param importRequest Used to indicate import from v1 is requested
         */
        data class OnboardingPreferences(val onboardingComplete: Boolean,
                                         val importComplete: Boolean,
                                         val importRequest: Boolean)

        /**
         * Wrapper over preferences stored in datastore
         */
        object PreferencesKey {
            val onboardingComplete = booleanPreferencesKey("onboarding_completed")
            val importComplete = booleanPreferencesKey("onboarding_import_completed")
            val importRequested = booleanPreferencesKey("onboarding_import_requested")
        }
    }

    private val _onboardingPrefs = MutableLiveData<OnboardingPreferences>()

    /**
     * Live data of onboarding preferences fetched from datastore
     */
    val onboardingPrefs: LiveData<OnboardingPreferences> = _onboardingPrefs

    init {
        viewModelScope.launch {
            // collecting from datastore and sending it via livedata using our wrapper class
            dataStore.data.onEach { Timber.d("Pref = $it") }.collect { preferences ->
                val onboardingCompleted = preferences[PreferencesKey.onboardingComplete]
                        ?: false
                val importCompleted = preferences[PreferencesKey.importComplete]
                        ?: false
                val importRequest = preferences[PreferencesKey.importRequested]
                        ?: false
                _onboardingPrefs.value = OnboardingPreferences(onboardingCompleted,
                        importCompleted, importRequest)
            }
        }
    }

    /**
     * User has pressed getStarted, onboarding completed
     */
    fun getStarted() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKey.onboardingComplete] = true
            }
        }
    }

    /**
     * Sets preferences to indicate import is requested
     */
    fun startImport() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKey.importRequested] = true
                preferences[PreferencesKey.onboardingComplete] = false
                preferences[PreferencesKey.importComplete] = false
            }
        }
    }

    /**
     * Reset import preferences
     */
    fun resetImportPrefs() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKey.importRequested] = false
            }
        }
    }

    /**
     * Clear all data from the database
     */
    @DangerousDatabase
    fun clearAll() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKey.onboardingComplete] = false
                preferences[PreferencesKey.importComplete] = false
                preferences[PreferencesKey.importRequested] = false
                repository.deleteAll()
            }
        }
    }
}
