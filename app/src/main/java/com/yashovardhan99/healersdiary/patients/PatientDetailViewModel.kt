package com.yashovardhan99.healersdiary.patients

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.yashovardhan99.healersdiary.create.CreateRepository
import com.yashovardhan99.healersdiary.database.Healing
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.database.Payment
import com.yashovardhan99.healersdiary.utils.*
import com.yashovardhan99.healersdiary.utils.HealingParent.Healing.Companion.toUiHealing
import com.yashovardhan99.healersdiary.utils.PaymentParent.Payment.Companion.toUiPayment
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedLastMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.earnedThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsLastMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsThisMonth
import com.yashovardhan99.healersdiary.utils.Stat.Companion.healingsToday
import com.yashovardhan99.healersdiary.utils.Stat.Companion.paymentDue
import com.yashovardhan99.healersdiary.utils.Utils.getHeading
import com.yashovardhan99.healersdiary.utils.Utils.insertSeparators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
        private val savedStateHandle: SavedStateHandle,
        private val repository: PatientDetailRepository,
        private val createRepository: CreateRepository) : ViewModel() {

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

    fun getHealings(patientId: Long): Flow<PagingData<HealingParent>> {
        return repository.getAllHealings(patientId).map { data ->
            data.map { healing -> healing.toUiHealing() }.insertSeparators { before: HealingParent.Healing?, after: HealingParent.Healing? ->
                if (before == null && after == null) null
                else if (before == null && after != null) HealingParent.HealingSeparator(getHeading(after.time))
                else if (before != null && after != null) {
                    if (getHeading(before.time) != getHeading(after.time)) HealingParent.HealingSeparator(getHeading(after.time))
                    else null
                } else null
            }
        }
    }

    fun getPayments(patientId: Long): Flow<PagingData<PaymentParent>> {
        return repository.getAllPayments(patientId).map { data ->
            data.map { it.toUiPayment() }
                    .insertSeparators { before, after ->
                        if (before == null && after == null) null
                        else if (before == null && after != null) PaymentParent.PaymentSeparator(getHeading(after.time))
                        else if (before != null && after != null) {
                            if (getHeading(before.time) != getHeading(after.time)) PaymentParent.PaymentSeparator(getHeading(after.time))
                            else null
                        } else null
                    }
        }
    }

    fun deleteHealing(healing: Healing) {
        val updatedPatient = patient.value?.let {
            it.copy(due = it.due - healing.charge)
        }
        savedStateHandle["deleted_healing"] = healing.toBundle()
        viewModelScope.launch {
            repository.deleteHealing(healing)
            updatedPatient?.let { repository.updatePatient(it) }
        }
    }

    private fun Healing.toBundle() = bundleOf(
            "id" to id,
            "time" to time.time,
            "charge" to charge,
            "notes" to notes,
            "pid" to patientId)

    private fun Bundle.toHealing(): Healing {
        val date = getLong("time", -1).let {
            if (it == -1L) Date()
            else Date(it)
        }
        return Healing(
                getLong("id"),
                date,
                getLong("charge"),
                getString("notes", ""),
                getLong("pid", patient.value?.id ?: 0))
    }

    fun undoDeleteHealing(): Boolean {
        val healing = savedStateHandle.get<Bundle>("deleted_healing")?.toHealing() ?: return false
        viewModelScope.launch {
            createRepository.insertNewHealing(healing)
        }
        return true
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