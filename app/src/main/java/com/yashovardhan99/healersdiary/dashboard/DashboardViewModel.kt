package com.yashovardhan99.healersdiary.dashboard

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedLastMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsToday
import com.yashovardhan99.healersdiary.utils.setToStartOfDay
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.*

class DashboardViewModel @ViewModelInject constructor(private val repository: DashboardRepository,
                                                      @ApplicationContext val context: Context) : ViewModel() {
    val patients = repository.patients
    private val thisMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
        setToStartOfDay()
    }
    private val lastMonth = (thisMonth.clone() as Calendar).apply {
        roll(Calendar.MONTH, false)
    }
    val dashboardStats = with(context) {
        listOf(healingsToday(0), healingsThisMonth(0), earnedThisMonth(0.0), earnedLastMonth(0.0))
    }

    init {
        Timber.d("This month = $thisMonth Last month = $lastMonth")
    }
}
