package com.yashovardhan99.core.database

import androidx.room.TypeConverter
import com.yashovardhan99.core.getLocalDateTimeFromMillis
import com.yashovardhan99.core.toEpochMilli
import java.time.LocalDateTime

class DateConverter {

    @TypeConverter
    fun timestampToLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let { getLocalDateTimeFromMillis(it) }
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
