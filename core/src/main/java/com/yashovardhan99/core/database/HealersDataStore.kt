package com.yashovardhan99.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.createDataStore
import com.yashovardhan99.core.database.OnboardingState.Companion.toOnboardingState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface HealersDataStore {
    fun getOnboardingState(): Flow<OnboardingState>
    suspend fun updateOnboardingState(state: OnboardingState)
}

sealed class OnboardingState(internal val value: Int) {
    object OnboardingRequired : OnboardingState(0)
    object ImportCompleted : OnboardingState(1)
    object OnboardingCompleted : OnboardingState(2)
    companion object {
        val PREF_KEY = intPreferencesKey("onboarding_state")
        fun Int.toOnboardingState(): OnboardingState = when (this) {
            0 -> OnboardingRequired
            1 -> ImportCompleted
            2 -> OnboardingCompleted
            else -> OnboardingRequired
        }
    }
}

class HealersDataStoreImpl @Inject constructor(@ApplicationContext context: Context) : HealersDataStore {
    private val dataStore: DataStore<Preferences> = context.createDataStore("healersDatastore")

    override fun getOnboardingState(): Flow<OnboardingState> {
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

    override suspend fun updateOnboardingState(state: OnboardingState) {
        dataStore.edit { preferences ->
            preferences[OnboardingState.PREF_KEY] = state.value
        }
    }

    companion object {
        /**
         * Onboarding preferences for datastore.
         * @param onboardingComplete Used to indicate whether onboarding has been completed
         * @param importComplete Used to indicate import from v1 is complete
         * @param importRequest Used to indicate import from v1 is requested
         */
        @Deprecated("Will be removed in later versions", level = DeprecationLevel.WARNING)
        private data class OnboardingPreferences(val onboardingComplete: Boolean,
                                                 val importComplete: Boolean,
                                                 val importRequest: Boolean)

        @Deprecated("Will be removed in later versions", level = DeprecationLevel.WARNING)
        private object PreferencesKey {
            val onboardingComplete = booleanPreferencesKey("onboarding_completed")
            val importComplete = booleanPreferencesKey("onboarding_import_completed")
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DatastoreModule {
    @Binds
    @Singleton
    abstract fun provideAppDatastore(healersDataStoreImpl: HealersDataStoreImpl): HealersDataStore

}