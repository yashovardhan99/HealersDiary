package com.yashovardhan99.healersdiary

import com.google.common.truth.Truth.assertThat
import com.yashovardhan99.core.*
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
}