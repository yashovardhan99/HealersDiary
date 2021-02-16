package com.yashovardhan99.healersdiary.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    private object Migration1to2 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE INDEX index_healings_time ON healings(time);")
            database.execSQL("CREATE INDEX index_healings_patient_id ON healings(patient_id);")
            database.execSQL("CREATE INDEX index_payments_time ON payments(time);")
            database.execSQL("CREATE INDEX index_payments_patient_id ON payments(patient_id);")
        }
    }

    @Provides
    @Singleton
    fun provideHealersDatabase(@ApplicationContext context: Context): HealersDatabase {
        return Room.databaseBuilder(context, HealersDatabase::class.java, "healers_db")
                .addMigrations(Migration1to2)
                .build()
    }

    @AppDataStore
    @Provides
    @Singleton
    fun provideAppDatastore(@ApplicationContext context: Context): DataStore<Preferences> = context.createDataStore("healersDatastore")
}