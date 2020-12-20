package com.yashovardhan99.healersdiary.dashboard

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.yashovardhan99.healersdiary.database.Healing
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.database.Payment
import com.yashovardhan99.healersdiary.utils.Activity
import com.yashovardhan99.healersdiary.utils.Request
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedLastMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsToday
import com.yashovardhan99.healersdiary.utils.setToStartOfDay
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val healings = repository.getHealingsStarting(lastMonth.time)
    private val payments = repository.getPaymentsStarting(lastMonth.time)

    val patientsList = healings.combine(patientsFlow) { healings, patients ->
        val patientsMap = patients.associateBy { it.id }
        Timber.d(patientsMap.toString())
        Timber.d("Healings 1 = $healings")
        val patientWithHealings = healings.groupBy {
            patientsMap[it.patientId] ?: Patient.MissingPatient
        }
        patients.map { patient ->
            val today = patientWithHealings[patient]?.count { it.time >= today.time } ?: 0
            val thisMonth = patientWithHealings[patient]?.count { it.time >= thisMonth.time } ?: 0
            patient.copy(healingsToday = today, healingsThisMonth = thisMonth)
        }.sortedByDescending { it.lastModified }
    }

    val dashboardFlow = healings
            .combine(patientsFlow) { healings, patients ->
                Pair(healings, patients)
            }
            .combine(payments) { healingsWithPatients: Pair<List<Healing>, List<Patient>>, payments: List<Payment> ->
                val healings = healingsWithPatients.first
                val patients = healingsWithPatients.second.associateBy { it.id }
                Timber.d("Healings = $healings")
                Timber.d("Patients = $patients")
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
    private val _requests = MutableStateFlow<Request?>(null)
    val requests: StateFlow<Request?> = _requests
    fun viewPatient(patientId: Long) {
        _requests.value = Request.ViewPatient(patientId)
    }

    fun resetRequest() {
        _requests.value = null
    }

    fun addNewPatient() {
        _requests.value = Request.NewPatient
    }

    init {
        Timber.d("DATES: Today = ${today.time} This month = ${thisMonth.time} Last month = ${lastMonth.time}")
    }
}