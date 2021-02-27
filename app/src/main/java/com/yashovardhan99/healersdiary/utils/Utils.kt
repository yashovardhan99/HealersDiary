package com.yashovardhan99.healersdiary.utils

import android.text.format.DateUtils
import com.yashovardhan99.core.setToStartOfMonth
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    private val thisWeek = Calendar.getInstance().apply {
        add(Calendar.WEEK_OF_YEAR, -1)
    }.time
    private val thisMonth = Calendar.getInstance().apply { setToStartOfMonth() }.time

    fun List<ActivityParent.Activity>.insertSeparators(): List<ActivityParent> {
        val listWithSeparator = mutableListOf<ActivityParent>()
        forEachIndexed { index, activity ->
            if (index == 0 || getHeading(activity.time) != getHeading(get(index - 1).time)) {
                listWithSeparator.add(ActivityParent.ActivitySeparator(getHeading(activity.time)))
            }
            listWithSeparator.add(activity)
        }
        return listWithSeparator
    }

    fun getHeading(date: Date): String {
        return when {
            date.after(thisWeek) -> DateUtils.getRelativeTimeSpanString(date.time, Date().time, DateUtils.DAY_IN_MILLIS).toString()
            !date.before(thisMonth) -> DateUtils.getRelativeTimeSpanString(date.time, Date().time, DateUtils.WEEK_IN_MILLIS).toString()
            else -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
        }
    }
}