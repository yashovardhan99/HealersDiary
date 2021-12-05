package com.yashovardhan99.core

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.util.*
import org.junit.Test

/**
 * Tests for DateTimeUtils.kt functions
 */
class DateTimeUtilsTest {

    @Test
    fun toLocalDateTime_utc() {
        val date = Date(1614198366089)
        val localDateTime = date.toLocalDateTime(ZoneId.of("UTC"))
        assertThat(localDateTime.dayOfMonth).isEqualTo(24)
        assertThat(localDateTime.month).isEquivalentAccordingToCompareTo(Month.FEBRUARY)
        assertThat(localDateTime.year).isEqualTo(2021)
        assertThat(localDateTime.hour).isEqualTo(20)
        assertThat(localDateTime.minute).isEqualTo(26)
        assertThat(localDateTime.second).isEqualTo(6)
        // verify date has not been changed
        assertThat(date.time).isEqualTo(1614198366089)
    }

    @Test
    fun toLocalDateTime_ist() {
        val date = Date(1614198366089)
        val localDateTime = date.toLocalDateTime(ZoneId.of("Asia/Kolkata"))
        // Asia/Kolkata is +5:30 hours ahead of UTC
        assertThat(localDateTime.dayOfMonth).isEqualTo(25)
        assertThat(localDateTime.month).isEquivalentAccordingToCompareTo(Month.FEBRUARY)
        assertThat(localDateTime.year).isEqualTo(2021)
        assertThat(localDateTime.hour).isEqualTo(1)
        assertThat(localDateTime.minute).isEqualTo(56)
        assertThat(localDateTime.second).isEqualTo(6)
    }

    @Test
    fun localDateTime_startMonth() {
        val localDate = LocalDate.of(2021, Month.FEBRUARY, 28)
        val startOfMonth = localDate.getStartOfMonth()
        assertThat(startOfMonth.dayOfMonth).isEqualTo(1)
        assertThat(startOfMonth.month).isEquivalentAccordingToCompareTo(Month.FEBRUARY)
        assertThat(startOfMonth.year).isEqualTo(2021)
    }

    @Test
    fun localDateTime_startLastMonth() {
        val localDate = LocalDate.of(2021, Month.MARCH, 31)
        val startOfMonth = localDate.getStartOfLastMonth()
        assertThat(startOfMonth.dayOfMonth).isEqualTo(1)
        assertThat(startOfMonth.month).isEquivalentAccordingToCompareTo(Month.FEBRUARY)
        assertThat(startOfMonth.year).isEqualTo(2021)
    }

    @Test
    fun localDateTime_toEpochMilli_utc() {
        val localDateTime = LocalDateTime.of(2021, Month.MAY, 12, 6, 55, 24)
        val millis = localDateTime.toEpochMilli(ZoneId.of("UTC"))
        assertThat(millis).isEqualTo(1620802524000)
    }

    @Test
    fun localDateTime_toEpochMilli_ist() {
        val localDateTime = LocalDateTime.of(2021, Month.MAY, 12, 12, 25, 24)
        val millis = localDateTime.toEpochMilli(ZoneId.of("Asia/Kolkata"))
        assertThat(millis).isEqualTo(1620802524000)
    }

    @Test
    fun instant_toLocalDateTime_utc() {
        val instant = Instant.ofEpochMilli(1620802524000)
        val localDateTime = instant.toLocalDateTime(ZoneId.of("UTC"))
        assertThat(localDateTime.year).isEqualTo(2021)
        assertThat(localDateTime.month).isEqualTo(Month.MAY)
        assertThat(localDateTime.dayOfMonth).isEqualTo(12)
        assertThat(localDateTime.hour).isEqualTo(6)
        assertThat(localDateTime.minute).isEqualTo(55)
        assertThat(localDateTime.second).isEqualTo(24)
    }

    @Test
    fun instant_toLocalDateTime_ist() {
        val instant = Instant.ofEpochMilli(1620802524000)
        val localDateTime = instant.toLocalDateTime(ZoneId.of("Asia/Kolkata"))
        assertThat(localDateTime.year).isEqualTo(2021)
        assertThat(localDateTime.month).isEqualTo(Month.MAY)
        assertThat(localDateTime.dayOfMonth).isEqualTo(12)
        assertThat(localDateTime.hour).isEqualTo(12)
        assertThat(localDateTime.minute).isEqualTo(25)
        assertThat(localDateTime.second).isEqualTo(24)
    }

    @Test
    fun localDateTime_formatDate_en_in() {
        Locale.setDefault(Locale("en", "IN"))
        val localDateTime = LocalDateTime.of(2021, Month.MAY, 12, 20, 8, 5)
        val formattedDate = localDateTime.formatDate()
        /* Note: Depending on which jdk is used, the output varies. So using these tests to ensure consistency. */
        assertThat(formattedDate).contains("12")
        assertThat(formattedDate).contains("May")
        assertThat(formattedDate).contains("2021")
    }

    @Test
    fun localDateTime_formatDate_fr_fr() {
        Locale.setDefault(Locale("fr", "FR"))
        val localDateTime = LocalDateTime.of(2021, Month.MAY, 12, 20, 8, 5)
        assertThat(localDateTime.formatDate()).isEqualTo("12 mai 2021")
    }

    @Test
    fun localDateTime_formatDate_en_us() {
        Locale.setDefault(Locale("en", "US"))
        val localDateTime = LocalDateTime.of(2021, Month.MAY, 12, 20, 8, 5)
        assertThat(localDateTime.formatDate()).isEqualTo("May 12, 2021")
    }

    @Test
    fun localDateTime_formatTime_en_in() {
        Locale.setDefault(Locale("en", "IN"))
        val localDateTime = LocalDateTime.of(2021, Month.MAY, 12, 20, 8, 5)
        assertThat(localDateTime.formatTime()).isEqualTo("8:08:05 PM")
    }

    @Test
    fun localDateTime_formatTime_fr_fr() {
        Locale.setDefault(Locale("fr", "FR"))
        val localDateTime = LocalDateTime.of(2021, Month.MAY, 12, 20, 8, 5)
        assertThat(localDateTime.formatTime()).isEqualTo("20:08:05")
    }
}