package com.yashovardhan99.healersdiary.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.room.Room
import com.yashovardhan99.healersdiary.AppDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    fun provideHealersDao(database: HealersDatabase): HealersDao {
        return database.healersDao()
    }

    @Provides
    @Singleton
    fun provideHealersDatabase(@ApplicationContext context: Context): HealersDatabase {
        return Room.databaseBuilder(context, HealersDatabase::class.java, "healers_db")
                .build()
    }

    @AppDataStore
    @Provides
    @Singleton
    fun provideAppDatastore(@ApplicationContext context: Context): DataStore<Preferences> = context.createDataStore("healersDatastore")
}