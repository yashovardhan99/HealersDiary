package com.yashovardhan99.healersdiary.database

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

class ActivityTypeConverter {
    @TypeConverter
    fun actTypeToString(activityType: ActivityType?): String? {
        return activityType?.type
    }

    @TypeConverter
    fun typeStringToActType(type: String?): ActivityType? {
        return type?.let { ActivityType.valueOf(it) }
    }
}
