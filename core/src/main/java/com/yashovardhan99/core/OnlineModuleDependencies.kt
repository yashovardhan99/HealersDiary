package com.yashovardhan99.core

import com.yashovardhan99.core.database.HealersDataStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OnlineModuleDependencies {
    fun dataStore(): HealersDataStore
}