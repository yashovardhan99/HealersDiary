package com.yashovardhan99.core.database

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import com.yashovardhan99.core.database.OnboardingState.Companion.toOnboardingState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

sealed class OnboardingState(internal val value: Int) {
    object OnboardingRequired : OnboardingState(0)
    object ImportCompleted : OnboardingState(1)
    object OnboardingCompleted : OnboardingState(2)
    object Importing : OnboardingState(3)
    companion object {
        val PREF_KEY = intPreferencesKey("onboarding_state")
        fun Int.toOnboardingState(): OnboardingState = when (this) {
            0 -> OnboardingRequired
            1 -> ImportCompleted
            2 -> OnboardingCompleted
            3 -> Importing
            else -> OnboardingRequired
        }

        const val IMPORT_COMPLETE_NOTIF_ID = 200
    }
}

@Singleton
class HealersDataStore @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore: DataStore<Preferences> = context.createDataStore("healersDatastore")

    fun getExportLocation(): Flow<Uri?> {
        return dataStore.data.map { preferences ->
            preferences[exportLocationKey]?.let {
                if (it.isBlank()) null
                else Uri.parse(it)
            }
        }
    }

    suspend fun updateExportLocation(uri: Uri?) {
        dataStore.edit { preferences ->
            Timber.d("Updating Export location = $uri")
            preferences[exportLocationKey] = uri?.toString() ?: ""
        }
    }

    fun getOnboardingState(): Flow<OnboardingState> {
        return dataStore.data.map { preferences ->
            preferences[OnboardingState.PREF_KEY]?.toOnboardingState()
                ?: migrateOnboardingDatastore(preferences)
        }
    }

    private fun migrateOnboardingDatastore(preferences: Preferences): OnboardingState {
        return when {
            preferences[PreferencesKey.importComplete]
                ?: false -> OnboardingState.ImportCompleted
            preferences[PreferencesKey.onboardingComplete]
                ?: false -> OnboardingState.OnboardingCompleted
            else -> OnboardingState.OnboardingRequired
        }
    }

    suspend fun updateOnboardingState(state: OnboardingState) {
        dataStore.edit { preferences ->
            preferences[OnboardingState.PREF_KEY] = state.value
        }
    }

    companion object {

        private val exportLocationKey = stringPreferencesKey("export_location")

        /**
         * Onboarding preferences for datastore.
         * @param onboardingComplete Used to indicate whether onboarding has been completed
         * @param importComplete Used to indicate import from v1 is complete
         * @param importRequest Used to indicate import from v1 is requested
         */
        @Deprecated("Will be removed in later versions", level = DeprecationLevel.WARNING)
        private data class OnboardingPreferences(
            val onboardingComplete: Boolean,
            val importComplete: Boolean,
            val importRequest: Boolean
        )

        @Deprecated("Will be removed in later versions", level = DeprecationLevel.WARNING)
        private object PreferencesKey {
            val onboardingComplete = booleanPreferencesKey("onboarding_completed")
            val importComplete = booleanPreferencesKey("onboarding_import_completed")
        }
    }
}
