package com.yashovardhan99.healersdiary.dashboard

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.yashovardhan99.healersdiary.database.Healing
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.database.Payment
import com.yashovardhan99.healersdiary.utils.Activity
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedLastMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsToday
import com.yashovardhan99.healersdiary.utils.setToStartOfDay
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import java.util.*

class DashboardViewModel @ViewModelInject constructor(private val repository: DashboardRepository,
                                                      @ApplicationContext val context: Context) : ViewModel() {
    private val patientsFlow = repository.patients
    private val today = Calendar.getInstance().apply {
        setToStartOfDay()
    }
    private val thisMonth = (today.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
    }
    private val lastMonth = (thisMonth.clone() as Calendar).apply {
        roll(Calendar.MONTH, false)
    }
    val dashboardFlow = repository.getHealingsStarting(lastMonth.time)
            .distinctUntilChanged().conflate()
            .combine(patientsFlow) { healings, patients ->
                Pair(healings, patients)
            }.conflate()
            .combine(repository.getPaymentsStarting(lastMonth.time).distinctUntilChanged().conflate()) { healingsWithPatients: Pair<List<Healing>, List<Patient>>, payments: List<Payment> ->
                val healings = healingsWithPatients.first
                val patients = healingsWithPatients.second.associateBy { it.id }
                val activities = healings.map { healing ->
                    Activity(healing.time, Activity.Type.HEALING(context), healing.charge, patients[healing.patientId]
                            ?: Patient.MissingPatient)
                } + payments.map { payment ->
                    Activity(payment.time, Activity.Type.PAYMENT(context), payment.amount, patients[payment.patientId]
                            ?: Patient.MissingPatient)
                }
                val healingsThisMonth = healings.filter { !it.time.before(thisMonth.time) }
                val healingsLastMonth = healings - healingsThisMonth
                val healingsToday = healings.filter { !it.time.before(today.time) }
                Timber.d("Last month = ${healingsLastMonth.size}")
                Timber.d("This month = ${healingsThisMonth.size}")
                Timber.d("Today = ${healingsToday.size}")
                val earningsThisMonth = healingsThisMonth.sumOf { it.charge }.toDouble() / 100
                val earningsLastMonth = healingsLastMonth.sumOf { it.charge }.toDouble() / 100
                Timber.d("Earnings this month = $earningsThisMonth")
                Timber.d("Earnings last month = $earningsLastMonth")
                val stats = with(context) {
                    listOf(healingsToday(healingsToday.size), healingsThisMonth(healingsThisMonth.size),
                            earnedThisMonth(earningsThisMonth), earnedLastMonth(earningsLastMonth))
                }
                if (activities.isNotEmpty()) Pair(stats, activities.sortedByDescending { it.time })
                else Pair(stats, null)
            }

    init {
        Timber.d("DATES: Today = ${today.time} This month = ${thisMonth.time} Last month = ${lastMonth.time}")
    }
}
