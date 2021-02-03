package com.yashovardhan99.healersdiary

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OnlineModuleDependencies {

    @AppDataStore
    fun dataStore(): DataStore<Preferences>

//    @AppFirebaseAuth
//    fun firebaseAuth(): FirebaseAuth
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppFirebaseAuth