package com.yashovardhan99.healersdiary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Patient::class, Healing::class, Payment::class],
        views = [Activity::class],
        version = 1)
@TypeConverters(DateConverter::class, ActivityTypeConverter::class)
abstract class HealersDatabase : RoomDatabase() {
    abstract fun healersDao(): HealersDao
}