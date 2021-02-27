package com.yashovardhan99.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OnlineModuleDependencies {
    @AppDataStore
    fun dataStore(): DataStore<Preferences>
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppDataStore