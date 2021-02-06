package com.yashovardhan99.healersdiary.online.importFirebase

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class ImportFirebaseModule {
    @Provides
    fun provideImportFirebaseViewModel(fragment: ImportFirebaseFragment) =
            ViewModelProvider(fragment).get(ImportFirebaseViewModel::class.java)

    @Provides
    fun provideContext(fragment: ImportFirebaseFragment): Context = fragment.requireContext().applicationContext
}