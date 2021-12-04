package com.yashovardhan99.core.utils

import android.text.format.DateUtils
import com.yashovardhan99.core.toEpochMilliAtDayStart
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import kotlin.experimental.ExperimentalTypeInference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

object Utils {
    private val today = LocalDate.now()
    private val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL)
        .appendLiteral(' ')
        .appendValue(ChronoField.YEAR)
        .toFormatter()

    fun getDateHeading(localDate: LocalDate): String {
        val period = Period.between(localDate, today)
        return when {
            period.days < 7 && today.month == localDate.month && today.year == localDate.year -> DateUtils.getRelativeTimeSpanString(
                localDate.toEpochMilliAtDayStart(),
                today.toEpochMilliAtDayStart(),
                DateUtils.DAY_IN_MILLIS
            ).toString()

            localDate.month == today.month && today.year == localDate.year -> DateUtils.getRelativeTimeSpanString(
                localDate.toEpochMilliAtDayStart(),
                today.toEpochMilliAtDayStart(),
                DateUtils.WEEK_IN_MILLIS
            ).toString()
            else -> localDate.format(formatter)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalTypeInference::class)
    fun <T1, T2, T3, T4, T5, T6, R> combineTransform(
        flow: Flow<T1>,
        flow2: Flow<T2>,
        flow3: Flow<T3>,
        flow4: Flow<T4>,
        flow5: Flow<T5>,
        flow6: Flow<T6>,
        @BuilderInference transform: suspend FlowCollector<R>.(T1, T2, T3, T4, T5, T6) -> Unit
    ): Flow<R> = kotlinx.coroutines.flow.combineTransform(
        flow,
        flow2,
        flow3,
        flow4,
        flow5,
        flow6
    ) { args: Array<*> ->
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6
        )
    }
}
