package com.yashovardhan99.core.utils

import android.text.format.DateUtils
import com.yashovardhan99.core.getStartOfMonth
import com.yashovardhan99.core.toDate
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.experimental.ExperimentalTypeInference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

object Utils {
    private val thisWeekDate = LocalDate.now().minusWeeks(1)
    private val thisMonthDate = LocalDate.now().getStartOfMonth()

    fun getHeading(date: Date): String {
        return when {
            date.after(thisWeekDate.toDate()) -> DateUtils.getRelativeTimeSpanString(
                date.time, Date().time, DateUtils.DAY_IN_MILLIS
            ).toString()

            !date.before(thisMonthDate.toDate()) -> DateUtils.getRelativeTimeSpanString(
                date.time,
                Date().time,
                DateUtils.WEEK_IN_MILLIS
            ).toString()
            else -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
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
