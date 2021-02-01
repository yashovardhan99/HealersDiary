package com.yashovardhan99.healersdiary.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.yashovardhan99.healersdiary.database.Healing
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.database.Payment
import com.yashovardhan99.healersdiary.utils.*
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedLastMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsLastMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsToday
import com.yashovardhan99.healersdiary.utils.Stat.Companion.paymentDue
import com.yashovardhan99.healersdiary.utils.Utils.insertSeparators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
        private val repository: PatientDetailRepository) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient
    private val todayDate = Calendar.getInstance().apply { setToStartOfDay() }
    private val thisMonthDate = Calendar.getInstance().apply { setToStartOfMonth() }
    private val lastMonthDate = Calendar.getInstance().apply { setToStartOfLastMonth() }

    fun setPatientId(patientId: Long) {
        viewModelScope.launch {
            _patient.emit(repository.getPatient(patientId))
        }
    }

    fun getStatWithActivities(patientId: Long): Flow<Pair<List<Stat>, List<ActivityParent>>> {
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
                    healingsToday(healingsToday),
                    paymentDue(paymentDue.toBigDecimal().movePointLeft(2)),
                    healingsThisMonth(healingsThisMonth),
                    earnedThisMonth(earningsThisMonth.toBigDecimal().movePointLeft(2).toDouble()),
                    healingsLastMonth(healingsLastMonth),
                    earnedLastMonth(earningsLastMonth.toBigDecimal().movePointLeft(2).toDouble())
            )
            Triple(stats, healings, patient)
        }.combine(paymentsFlow) { triplet, payments ->
            val stats = triplet.first
            val healings = triplet.second
            val patient = triplet.third
            val activities = healings.map { healing ->
                ActivityParent.Activity(healing.id, healing.time, ActivityParent.Activity.Type.HEALING, healing.charge, patient
                        ?: Patient.MissingPatient)
            } + payments.map { payment ->
                ActivityParent.Activity(payment.id, payment.time, ActivityParent.Activity.Type.PAYMENT, payment.amount, patient
                        ?: Patient.MissingPatient)
            }
            Pair(stats, activities.sortedByDescending { it.time }.insertSeparators())
        }
    }

    fun getHealings(patientId: Long): Flow<PagingData<Healing>> {
        return repository.getAllHealings(patientId)
    }

    fun getPayments(patientId: Long): Flow<PagingData<Payment>> {
        return repository.getAllPayments(patientId)
    }

    fun deleteHealing(healing: Healing) {
        val updatedPatient = patient.value?.let {
            it.copy(due = it.due - healing.charge)
        }
        viewModelScope.launch {
            repository.deleteHealing(healing)
            updatedPatient?.let { repository.updatePatient(it) }
        }
    }

    fun deletePayment(payment: Payment) {
        val updatedPatient = patient.value?.let {
            it.copy(due = it.due + payment.amount)
        }
        viewModelScope.launch {
            repository.deletePayment(payment)
            updatedPatient?.let { repository.updatePatient(it) }
        }
    }
}