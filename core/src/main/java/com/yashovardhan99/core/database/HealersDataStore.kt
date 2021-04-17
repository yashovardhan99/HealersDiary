package com.yashovardhan99.core.database

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import com.yashovardhan99.core.database.OnboardingState.Companion.toOnboardingState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
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

sealed class BackupState(internal val value: Int) {
    object Unknown : BackupState(0)
    object Running : BackupState(1)
    class LastRunSuccess(
        val instant: Instant,
        val backedUp: IntArray,
        val exportFolder: Uri
    ) : BackupState(2) {
        companion object {
            val TimePrefKey = longPreferencesKey("backup_time")
            val BackupPrefKeys = arrayOf(
                intPreferencesKey("backup_patients"),
                intPreferencesKey("backup_healings"),
                intPreferencesKey("backup_payments")
            )
            val locationPrefKey = stringPreferencesKey("last_backup_location")
        }
    }

    class LastRunFailed(val instant: Instant) : BackupState(3) {
        companion object {
            val TimePrefKey = longPreferencesKey("backup_time")
        }
    }

    companion object {
        val PrefKey = intPreferencesKey("backup_state")
    }
}

@Singleton
class HealersDataStore @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore: DataStore<Preferences> = context.createDataStore("healersDatastore")

    fun getBackupState(): Flow<BackupState> {
        return dataStore.data.map { preferences ->
            when (preferences[BackupState.PrefKey]) {
                1 -> BackupState.Running
                2 -> BackupState.LastRunSuccess(
                    preferences[BackupState.LastRunSuccess.TimePrefKey]?.let {
                        Instant.ofEpochMilli(it)
                    } ?: Instant.EPOCH,
                    IntArray(3) { idx ->
                        preferences[BackupState.LastRunSuccess.BackupPrefKeys[idx]] ?: 0
                    },
                    Uri.parse(preferences[BackupState.LastRunSuccess.locationPrefKey] ?: "")
                )
                3 -> BackupState.LastRunFailed(
                    preferences[BackupState.LastRunSuccess.TimePrefKey]?.let {
                        Instant.ofEpochMilli(it)
                    } ?: Instant.EPOCH
                )
                else -> BackupState.Unknown
            }
        }
    }

    suspend fun updateBackupState(state: BackupState) {
        dataStore.edit { preferences ->
            Timber.d("Updating backup state = $state")
            preferences[BackupState.PrefKey] = state.value
            when (state) {
                is BackupState.LastRunFailed -> {
                    preferences[BackupState.LastRunFailed.TimePrefKey] =
                        state.instant.toEpochMilli()
                }
                is BackupState.LastRunSuccess -> {
                    preferences[BackupState.LastRunSuccess.TimePrefKey] =
                        state.instant.toEpochMilli()
                    check(state.backedUp.size == 3)
                    state.backedUp.forEachIndexed { index, count ->
                        preferences[BackupState.LastRunSuccess.BackupPrefKeys[index]] = count
                    }
                    preferences[BackupState.LastRunSuccess.locationPrefKey] =
                        state.exportFolder.toString()
                }
                else -> Unit
            }
        }
    }

    fun getExportLocation(): Flow<Uri?> {
        return dataStore.data.map { preferences ->
            preferences[exportLocationKey].let {
                if (it.isNullOrBlank()) null
                else Uri.parse(it)
            }
        }
    }

    suspend fun updateExportLocation(uri: Uri?) {
        dataStore.edit { preferences ->
            Timber.d("Updating Export location = $uri")
            preferences[exportLocationKey] = uri.toString()
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
