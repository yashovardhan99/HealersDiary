package com.yashovardhan99.healersdiary.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashovardhan99.core.DangerousDatabase
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.database.HealersDataStore
import com.yashovardhan99.core.database.OnboardingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Viewmodel used by Splash screen in onboarding flow
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: HealersDataStore,
    private val repository: OnboardingRepository
) : ViewModel() {
    /**
     * Live data of onboarding preferences fetched from datastore
     */
    private val _onboardingPrefs =
        MutableStateFlow<OnboardingState>(OnboardingState.Fetching)
    val onboardingPrefs: StateFlow<OnboardingState> = _onboardingPrefs

    private val _importRequested = MutableStateFlow(false)
    val importRequested = _importRequested.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.getOnboardingState().collect { onboardingState ->
                _onboardingPrefs.value = onboardingState
            }
        }
    }

    /**
     * User has pressed getStarted, onboarding completed
     */
    fun getStarted() {
        AnalyticsEvent.Onboarding.Completed.trackEvent()
        viewModelScope.launch {
            dataStore.updateOnboardingState(OnboardingState.OnboardingCompleted)
        }
    }

    /**
     * Sets preferences to indicate import is requested
     */
    fun startImport() {
        viewModelScope.launch {
            _importRequested.emit(true)
            dataStore.updateOnboardingState(OnboardingState.OnboardingRequired)
        }
    }

    fun resetImport() {
        viewModelScope.launch {
            _importRequested.emit(false)
        }
    }

    /**
     * Clear all data from the database
     */
    @DangerousDatabase
    fun clearAll() {
        AnalyticsEvent.Onboarding.ClearAll.trackEvent()
        viewModelScope.launch {
            dataStore.updateOnboardingState(OnboardingState.OnboardingRequired)
            repository.deleteAll()
        }
    }
}
