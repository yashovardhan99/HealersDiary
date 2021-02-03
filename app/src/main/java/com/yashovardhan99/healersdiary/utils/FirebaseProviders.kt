package com.yashovardhan99.healersdiary.utils

import com.google.firebase.auth.FirebaseAuth
import com.yashovardhan99.healersdiary.AppFirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FirebaseProviders {

//    @AppFirebaseAuth
//    @Provides
//    @Singleton
//    fun provideFirebaseAuth() = FirebaseAuth.getInstance()
}