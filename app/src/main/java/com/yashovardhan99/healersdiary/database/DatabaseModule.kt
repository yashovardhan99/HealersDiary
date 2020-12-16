package com.yashovardhan99.healersdiary.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
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
}