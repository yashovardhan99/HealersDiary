package com.yashovardhan99.healersdiary.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashovardhan99.core.DangerousDatabase
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.database.HealersDataStore
import com.yashovardhan99.core.database.OnboardingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Viewmodel used by Splash screen in onboarding flow
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(private val dataStore: HealersDataStore,
                                              private val repository: OnboardingRepository) : ViewModel() {


    /**
     * Live data of onboarding preferences fetched from datastore
     */
    private val _onboardingPrefs =
            MutableStateFlow<OnboardingState>(OnboardingState.OnboardingRequired)
    val onboardingPrefs: StateFlow<OnboardingState> = _onboardingPrefs

    private val _importRequested: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val importRequested = _importRequested.asSharedFlow()

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
    suspend fun startImport() {
        _importRequested.emit(true)
        dataStore.updateOnboardingState(OnboardingState.OnboardingRequired)
    }

    /**
     * Clear all data from the database
     */
    @DangerousDatabase
    suspend fun clearAll() {
        AnalyticsEvent.Onboarding.ClearAll.trackEvent()
        dataStore.updateOnboardingState(OnboardingState.OnboardingRequired)
        repository.deleteAll()
    }
}
