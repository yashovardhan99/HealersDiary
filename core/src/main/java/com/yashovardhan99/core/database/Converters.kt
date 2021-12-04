package com.yashovardhan99.core.database

import androidx.room.TypeConverter
import com.yashovardhan99.core.toEpochMilli
import com.yashovardhan99.core.toLocalDateTime
import java.time.Instant
import java.time.LocalDateTime
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
        return value?.let { Instant.ofEpochMilli(it).toLocalDateTime() }
    }

    @TypeConverter
    fun localDateTimeToTimestamp(dateTime: LocalDateTime?): Long? {
        return dateTime?.toEpochMilli()
    }
}

class ActivityTypeConverter {
    @TypeConverter
    fun actTypeToString(activityType: ActivityType?): String? {
        return activityType?.type
    }

    @TypeConverter
    fun typeStringToActType(type: String?): ActivityType? {
        return when (type) {
            ActivityType.HEALING.type -> ActivityType.HEALING
            ActivityType.PAYMENT.type -> ActivityType.PAYMENT
            ActivityType.PATIENT.type -> ActivityType.PATIENT
            else -> null
        }
    }
}
