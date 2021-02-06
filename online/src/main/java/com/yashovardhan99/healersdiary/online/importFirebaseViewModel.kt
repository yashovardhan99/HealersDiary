package com.yashovardhan99.healersdiary.online

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.yashovardhan99.healersdiary.AppDataStore
import com.yashovardhan99.healersdiary.onboarding.OnboardingViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ImportFirebaseViewModel @Inject constructor(
        @AppDataStore private val dataStore: DataStore<Preferences>) : ViewModel() {

    fun importCompleted() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[OnboardingViewModel.Companion.PreferencesKey.onboardingComplete] = true
                Timber.d("Preferences changed $preferences $dataStore")
            }
        }
    }

    fun setUser(currentUser: FirebaseUser) {
        Timber.d("Found user = $currentUser")
    }
}