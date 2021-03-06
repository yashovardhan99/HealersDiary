package com.yashovardhan99.core

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

fun Calendar.setToStartOfDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Calendar.setToStartOfMonth() {
    setToStartOfDay()
    set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
}

fun Calendar.setToStartOfLastMonth() {
    setToStartOfMonth()
    add(Calendar.MONTH, -1)
}

fun LocalDateTime.getStartOfDay() = truncatedTo(ChronoUnit.DAYS)

fun Date.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime = toInstant()
        .atZone(zoneId).toLocalDateTime()

fun LocalDateTime.toDate(zoneId: ZoneId = ZoneId.systemDefault()): Date = Date.from(atZone(zoneId)
        .toInstant())
