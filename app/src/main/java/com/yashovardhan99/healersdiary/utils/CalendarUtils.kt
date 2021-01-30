package com.yashovardhan99.healersdiary.utils

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