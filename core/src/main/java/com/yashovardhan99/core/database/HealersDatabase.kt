package com.yashovardhan99.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Patient::class, Healing::class, Payment::class],
    views = [Activity::class],
    version = 4
)
@TypeConverters(DateConverter::class, ActivityTypeConverter::class)
abstract class HealersDatabase : RoomDatabase() {
    abstract fun healersDao(): HealersDao
}
