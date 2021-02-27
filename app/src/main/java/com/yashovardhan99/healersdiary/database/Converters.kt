package com.yashovardhan99.healersdiary.database

import androidx.room.TypeConverter
import com.yashovardhan99.core.toLocalDateTime
import java.time.LocalDateTime
import java.time.ZoneId
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

    @TypeConverter
    fun timestampToLocalDateTime(value: Long?): LocalDateTime? {
        return fromTimestamp(value)?.toLocalDateTime()
    }

    @TypeConverter
    fun localDateTimeToTimestamp(dateTime: LocalDateTime?): Long? {
        return dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
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
