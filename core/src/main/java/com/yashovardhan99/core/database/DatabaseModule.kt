package com.yashovardhan99.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    private object Migration2to3 : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP VIEW IF EXISTS `Activity`;")
            database.execSQL(
                "CREATE VIEW `Activity` AS " +
                    "SELECT id, time, charge AS amount, notes, patient_id, 'healing' as type " +
                    "FROM healings " +
                    "UNION " +
                    "SELECT id, time, amount, notes, patient_id, 'payment' as type " +
                    "FROM payments " +
                    "ORDER BY time DESC;"
            )
        }
    }

    private object Migration3to4 : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP VIEW IF EXISTS `Activity`;")
            database.execSQL(
                "CREATE VIEW `Activity` AS " +
                    "SELECT id, time, charge AS amount, notes, patient_id, 'healing' as type " +
                    "FROM healings " +
                    "UNION " +
                    "SELECT id, time, amount, notes, patient_id, 'payment' as type " +
                    "FROM payments " +
                    "UNION " +
                    "SELECT id, created as time, due as amount, name as notes, " +
                    "id as patient_id, 'patient' AS type " +
                    "FROM patients " +
                    "ORDER BY time DESC"
            )
        }
    }

    @Provides
    @Singleton
    fun provideHealersDatabase(@ApplicationContext context: Context): HealersDatabase {
        return Room.databaseBuilder(context, HealersDatabase::class.java, "healers_db")
            .addMigrations(Migration1to2)
            .addMigrations(Migration2to3)
            .addMigrations(Migration3to4)
            .build()
    }
}
