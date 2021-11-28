package com.yashovardhan99.core

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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

fun LocalDate.getStartOfMonth(): LocalDate = withDayOfMonth(1)
fun LocalDate.getStartOfLastMonth(): LocalDate = getStartOfMonth().minusMonths(1)

fun Date.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime = toInstant()
    .atZone(zoneId).toLocalDateTime()

fun LocalDateTime.toDate(zoneId: ZoneId = ZoneId.systemDefault()): Date = Date.from(
    atZone(zoneId).toInstant()
)

fun LocalDate.toDate(zoneId: ZoneId = ZoneId.systemDefault()): Date = atTime(LocalTime.MIN)
    .toDate(zoneId)

fun LocalDateTime.formatDate() = toLocalDate().formatDate()
fun LocalDateTime.formatTime() = toLocalTime().formatTime()

fun LocalDate.formatDate(): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(this)

fun LocalTime.formatTime(): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(this)

fun LocalDateTime.toEpochMilli(zoneId: ZoneId = ZoneId.systemDefault()) = atZone(zoneId)
    .toInstant().toEpochMilli()

fun Instant.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime = atZone(zoneId)
    .toLocalDateTime()