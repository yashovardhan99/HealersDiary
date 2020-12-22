package com.yashovardhan99.healersdiary.patients

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.yashovardhan99.healersdiary.database.Healing
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.database.Payment
import com.yashovardhan99.healersdiary.utils.Activity
import com.yashovardhan99.healersdiary.utils.Stat
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedLastMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsLastMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsToday
import com.yashovardhan99.healersdiary.utils.Stat.Companion.paymentDue
import com.yashovardhan99.healersdiary.utils.setToStartOfDay
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*

class PatientDetailViewModel @ViewModelInject constructor(
        private val repository: PatientDetailRepository,
        @ApplicationContext private val context: Context) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient
    private val todayDate = Calendar.getInstance().apply { setToStartOfDay() }
    private val thisMonthDate = (todayDate.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
    }
    private val lastMonthDate = (thisMonthDate.clone() as Calendar).apply {
        roll(Calendar.MONTH, false)
    }

    fun setPatientId(patientId: Long) {
        viewModelScope.launch {
            _patient.emit(repository.getPatient(patientId))
        }
    }

    fun getStatWithActivities(patientId: Long): Flow<Pair<List<Stat>, List<Activity>>> {
        val healingsFlow = repository.getHealings(patientId, lastMonthDate.time)
        val paymentsFlow = repository.getPayments(patientId, lastMonthDate.time)
        return healingsFlow.combine(patient) { healings, patient ->
            val healingsToday = healings.count { it.time >= todayDate.time }
            val healingsThisMonth = healings.count { it.time >= thisMonthDate.time }
            val healingsLastMonth = healings.size - healingsThisMonth
            val earningsThisMonth = healings.filter { it.time >= thisMonthDate.time }.sumOf { it.charge }
            val earningsLastMonth = healings.filter { it.time < thisMonthDate.time }.sumOf { it.charge }
            val paymentDue = patient?.due ?: 0
            val stats = listOf(
                    context.healingsToday(healingsToday),
                    context.paymentDue(paymentDue.toBigDecimal().movePointLeft(2)),
                    context.healingsThisMonth(healingsThisMonth),
                    context.earnedThisMonth(earningsThisMonth.toBigDecimal().movePointLeft(2).toDouble()),
                    context.healingsLastMonth(healingsLastMonth),
                    context.earnedLastMonth(earningsLastMonth.toBigDecimal().movePointLeft(2).toDouble())
            )
            Triple(stats, healings, patient)
        }.combine(paymentsFlow) { triplet, payments ->
            val stats = triplet.first
            val healings = triplet.second
            val patient = triplet.third
            val activities = healings.map { healing ->
                Activity(healing.time, Activity.Type.HEALING(context), healing.charge, patient
                        ?: Patient.MissingPatient)
            } + payments.map { payment ->
                Activity(payment.time, Activity.Type.PAYMENT(context), payment.amount, patient
                        ?: Patient.MissingPatient)
            }
            Pair(stats, activities.sortedByDescending { it.time })
        }
    }

    fun getHealings(patientId: Long): Flow<PagingData<Healing>> {
        return repository.getAllHealings(patientId)
    }

    fun getPayments(patientId: Long): Flow<PagingData<Payment>> {
        return repository.getAllPayments(patientId)
    }
}