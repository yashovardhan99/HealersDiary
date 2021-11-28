package com.yashovardhan99.core

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.LocalDate
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.util.*

/**
 * Tests for CalendarUtils.kt functions
 */
class CalendarUtilsTest {
    @Test
    fun startOfDay_isValid() {
        val calendar = Calendar.getInstance().apply {
            set(2021, 2, 25, 1, 28, 12)
            setToStartOfDay()
        }
        assertThat(calendar[Calendar.HOUR_OF_DAY]).isEqualTo(0)
        assertThat(calendar[Calendar.MINUTE]).isEqualTo(0)
        assertThat(calendar[Calendar.SECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.MILLISECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.DATE]).isEqualTo(25)
        assertThat(calendar[Calendar.MONTH]).isEqualTo(2)
        assertThat(calendar[Calendar.YEAR]).isEqualTo(2021)
    }

    @Test
    fun startOfMonth_middleMonth() {
        val calendar = Calendar.getInstance().apply {
            set(2021, 5, 23, 5, 20, 45)
            setToStartOfMonth()
        }
        assertThat(calendar[Calendar.HOUR_OF_DAY]).isEqualTo(0)
        assertThat(calendar[Calendar.MINUTE]).isEqualTo(0)
        assertThat(calendar[Calendar.SECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.MILLISECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.DATE]).isEqualTo(1)
        assertThat(calendar[Calendar.MONTH]).isEqualTo(5)
        assertThat(calendar[Calendar.YEAR]).isEqualTo(2021)
    }

    @Test
    fun startOfMonth_lastDay() {
        val calendar = Calendar.getInstance().apply {
            set(2021, 11, 31, 5, 20, 45)
            setToStartOfMonth()
        }
        assertThat(calendar[Calendar.HOUR_OF_DAY]).isEqualTo(0)
        assertThat(calendar[Calendar.MINUTE]).isEqualTo(0)
        assertThat(calendar[Calendar.SECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.MILLISECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.DATE]).isEqualTo(1)
        assertThat(calendar[Calendar.MONTH]).isEqualTo(11)
        assertThat(calendar[Calendar.YEAR]).isEqualTo(2021)
    }

    @Test
    fun startOfMonth_firstDay() {
        val calendar = Calendar.getInstance().apply {
            set(2021, 0, 1, 5, 20, 45)
            setToStartOfMonth()
        }
        assertThat(calendar[Calendar.HOUR_OF_DAY]).isEqualTo(0)
        assertThat(calendar[Calendar.MINUTE]).isEqualTo(0)
        assertThat(calendar[Calendar.SECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.MILLISECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.DATE]).isEqualTo(1)
        assertThat(calendar[Calendar.MONTH]).isEqualTo(0)
        assertThat(calendar[Calendar.YEAR]).isEqualTo(2021)
    }


    @Test
    fun lastMonth_lastMonth() {
        val calendar = Calendar.getInstance().apply {
            set(2021, 11, 31, 5, 20, 45)
            setToStartOfLastMonth()
        }
        assertThat(calendar[Calendar.HOUR_OF_DAY]).isEqualTo(0)
        assertThat(calendar[Calendar.MINUTE]).isEqualTo(0)
        assertThat(calendar[Calendar.SECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.MILLISECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.DATE]).isEqualTo(1)
        assertThat(calendar[Calendar.MONTH]).isEqualTo(10)
        assertThat(calendar[Calendar.YEAR]).isEqualTo(2021)
    }

    @Test
    fun lastMonth_middleMonth() {
        val calendar = Calendar.getInstance().apply {
            set(2021, 6, 15, 5, 20, 45)
            setToStartOfLastMonth()
        }
        assertThat(calendar[Calendar.HOUR_OF_DAY]).isEqualTo(0)
        assertThat(calendar[Calendar.MINUTE]).isEqualTo(0)
        assertThat(calendar[Calendar.SECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.MILLISECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.DATE]).isEqualTo(1)
        assertThat(calendar[Calendar.MONTH]).isEqualTo(5)
        assertThat(calendar[Calendar.YEAR]).isEqualTo(2021)
    }

    @Test
    fun lastMonth_startingMonth() {
        val calendar = Calendar.getInstance().apply {
            set(2021, 0, 15, 5, 20, 45)
            setToStartOfLastMonth()
        }
        assertThat(calendar[Calendar.HOUR_OF_DAY]).isEqualTo(0)
        assertThat(calendar[Calendar.MINUTE]).isEqualTo(0)
        assertThat(calendar[Calendar.SECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.MILLISECOND]).isEqualTo(0)
        assertThat(calendar[Calendar.DATE]).isEqualTo(1)
        assertThat(calendar[Calendar.MONTH]).isEqualTo(11)
        assertThat(calendar[Calendar.YEAR]).isEqualTo(2020)
    }

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
    fun toDate_utc() {
        val localDateTime = LocalDateTime.of(2020, Month.MAY, 12, 8, 26, 23)
        val date = localDateTime.toDate(ZoneId.of("UTC"))
        assertThat(date.time).isEqualTo(1589271983000)
    }

    @Test
    fun toDate_ist() {
        val localDateTime = LocalDateTime.of(2020, Month.MAY, 12, 13, 56, 23)
        val date = localDateTime.toDate(ZoneId.of("Asia/Kolkata"))
        assertThat(date.time).isEqualTo(1589271983000)
    }

    @Test
    fun localDateTime_startDate() {
        val localDateTime = LocalDateTime.of(2021, Month.FEBRUARY, 28, 18, 31, 10, 100)
        val startOfDay = localDateTime.getStartOfDay()
        val time = startOfDay.toLocalTime()
        assertThat(time.toNanoOfDay()).isEqualTo(0)
        assertThat(startOfDay.dayOfMonth).isEqualTo(28)
        assertThat(startOfDay.month).isEquivalentAccordingToCompareTo(Month.FEBRUARY)
        assertThat(startOfDay.year).isEqualTo(2021)
    }

    @Test
    fun localDateTime_startDate_endTime() {
        val localDateTime = LocalDateTime.of(2021, Month.FEBRUARY, 28, 23, 59, 59, 999_999_999)
        val startOfDay = localDateTime.getStartOfDay()
        val time = startOfDay.toLocalTime()
        assertThat(time.toNanoOfDay()).isEqualTo(0)
        assertThat(startOfDay.dayOfMonth).isEqualTo(28)
        assertThat(startOfDay.month).isEquivalentAccordingToCompareTo(Month.FEBRUARY)
        assertThat(startOfDay.year).isEqualTo(2021)
    }

    @Test
    fun localDateTime_startDate_startTime() {
        val localDateTime = LocalDateTime.of(2021, Month.FEBRUARY, 28, 0, 0, 0)
        val startOfDay = localDateTime.getStartOfDay()
        val time = startOfDay.toLocalTime()
        assertThat(time.toNanoOfDay()).isEqualTo(0)
        assertThat(startOfDay).isEquivalentAccordingToCompareTo(localDateTime)
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
        assertThat(localDateTime.formatDate()).isEqualTo("12-May-2021")
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