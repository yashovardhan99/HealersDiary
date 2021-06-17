package com.yashovardhan99.healersdiary.patients

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import com.yashovardhan99.core.database.toHealing
import com.yashovardhan99.core.database.toPayment
import com.yashovardhan99.core.setToStartOfDay
import com.yashovardhan99.core.setToStartOfLastMonth
import com.yashovardhan99.core.setToStartOfMonth
import com.yashovardhan99.core.toLocalDateTime
import com.yashovardhan99.core.utils.ActivityParent
import com.yashovardhan99.core.utils.ActivityParent.Activity.Companion.getSeparator
import com.yashovardhan99.core.utils.HealingParent
import com.yashovardhan99.core.utils.HealingParent.Healing.Companion.toUiHealing
import com.yashovardhan99.core.utils.PaymentParent
import com.yashovardhan99.core.utils.PaymentParent.Payment.Companion.toUiPayment
import com.yashovardhan99.core.utils.Stat.Companion.earnedLastMonth
import com.yashovardhan99.core.utils.Stat.Companion.earnedThisMonth
import com.yashovardhan99.core.utils.Stat.Companion.healingsLastMonth
import com.yashovardhan99.core.utils.Stat.Companion.healingsThisMonth
import com.yashovardhan99.core.utils.Stat.Companion.healingsToday
import com.yashovardhan99.core.utils.Stat.Companion.paymentDue
import com.yashovardhan99.core.utils.Utils.combineTransform
import com.yashovardhan99.core.utils.Utils.getHeading
import com.yashovardhan99.healersdiary.create.CreateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: PatientDetailRepository,
    private val createRepository: CreateRepository
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: Flow<Patient?> = _patient.distinctUntilChanged { old, new ->
        old == new
    }
    private val today = Calendar.getInstance().apply { setToStartOfDay() }
    private val thisMonth = Calendar.getInstance().apply { setToStartOfMonth() }
    private val lastMonth = Calendar.getInstance().apply { setToStartOfLastMonth() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val activities = patient.filterNotNull().flatMapLatest { patient ->
        repository.getActivities(patientId = patient.id)
            .map { pagingData ->
                pagingData.map { it.toUiActivity(patient) }
                    .insertSeparators { before: ActivityParent.Activity?,
                        after: ActivityParent.Activity? ->
                        getSeparator(before, after)
                    }
            }
    }.onEmpty { emit(PagingData.empty()) }.cachedIn(viewModelScope)

    private val todayDate = today.time.toLocalDateTime().toLocalDate()
    private val thisMonthDate = thisMonth.time.toLocalDateTime().toLocalDate()
    private val lastMonthDate = lastMonth.time.toLocalDateTime().toLocalDate()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val healingsToday = patient.filterNotNull().flatMapLatest { patient ->
        repository.getHealingCountBetween(todayDate, todayDate, patient.id)
    }.onStart { emit(0) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val healingsThisMonth = patient.filterNotNull().flatMapLatest { patient ->
        repository.getHealingCountBetween(thisMonthDate, todayDate, patient.id)
    }.onStart { emit(0) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val healingsLastMonth = patient.filterNotNull().flatMapLatest { patient ->
        repository.getHealingCountBetween(
            lastMonthDate,
            thisMonthDate.minusDays(1),
            patient.id
        )
    }.onStart { emit(0) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val earningsThisMonth = patient.filterNotNull().flatMapLatest { patient ->
        repository.getHealingAmountBetween(thisMonthDate, todayDate, patient.id)
    }.onStart { emit(0L) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val earningsLastMonth = patient.filterNotNull().flatMapLatest { patient ->
        repository.getHealingAmountBetween(
            lastMonthDate,
            thisMonthDate.minusDays(1),
            patient.id
        )
    }.onStart { emit(0L) }
    val stats = combineTransform(
        patient.mapNotNull { it?.due }, healingsToday, healingsThisMonth,
        healingsLastMonth, earningsThisMonth, earningsLastMonth
    ) { paymentDueAmount, healingsTodayCount, healingsThisMonthCount,
        healingsLastMonthCount, earningsThisMonth, earningsLastMonth ->
        val stats = listOf(
            healingsToday(healingsTodayCount),
            paymentDue(paymentDueAmount.toBigDecimal().movePointLeft(2)),
            healingsThisMonth(healingsThisMonthCount),
            earnedThisMonth(earningsThisMonth.toBigDecimal().movePointLeft(2)),
            healingsLastMonth(healingsLastMonthCount),
            earnedLastMonth(earningsLastMonth.toBigDecimal().movePointLeft(2))
        )
        emit(stats)
    }.onStart {
        val emptyStats = listOf(
            healingsToday(0),
            paymentDue(BigDecimal.ZERO),
            healingsThisMonth(0),
            earnedThisMonth(BigDecimal.ZERO),
            healingsLastMonth(0),
            earnedLastMonth(BigDecimal.ZERO)
        )
        emit(emptyStats)
    }.distinctUntilChanged()

    fun setPatientId(patientId: Long) {
        viewModelScope.launch {
            _patient.emit(repository.getPatient(patientId))
        }
    }

    fun getHealings(patientId: Long): Flow<PagingData<HealingParent>> {
        return repository.getAllHealings(patientId).map { data ->
            data.map { healing -> healing.toUiHealing() }
                .insertSeparators { before: HealingParent.Healing?, after: HealingParent.Healing? ->
                    if (before == null && after == null) null
                    else if (before == null && after != null) HealingParent.HealingSeparator(
                        getHeading(after.time)
                    )
                    else if (before != null && after != null) {
                        if (getHeading(before.time) != getHeading(after.time))
                            HealingParent.HealingSeparator(
                                getHeading(after.time)
                            )
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
                    else if (before == null && after != null) PaymentParent.PaymentSeparator(
                        getHeading(after.time)
                    )
                    else if (before != null && after != null) {
                        if (getHeading(before.time) != getHeading(after.time))
                            PaymentParent.PaymentSeparator(
                                getHeading(after.time)
                            )
                        else null
                    } else null
                }
        }
    }

    fun deleteHealing(healing: Healing) {
        savedStateHandle["deleted_healing"] = healing.toBundle()
        viewModelScope.launch {
            repository.deleteHealing(healing)
            AnalyticsEvent.Content.Healing(healing.patientId).trackDelete()
        }
    }

    fun undoDeleteHealing(): Boolean {
        val healing = savedStateHandle.remove<Bundle>("deleted_healing")?.toHealing()
            ?: return false
        viewModelScope.launch {
            createRepository.insertNewHealing(healing)
            AnalyticsEvent.Content.Healing(healing.patientId).trackUndoDelete()
        }
        return true
    }

    fun deletePayment(payment: Payment) {
        savedStateHandle["deleted_payment"] = payment.toBundle()
        viewModelScope.launch {
            repository.deletePayment(payment)
            AnalyticsEvent.Content.Payment(payment.patientId).trackDelete()
        }
    }

    fun undoDeletePayment(): Boolean {
        val payment = savedStateHandle.remove<Bundle>("deleted_payment")?.toPayment()
            ?: return false
        viewModelScope.launch {
            createRepository.insertNewPayment(payment)
            AnalyticsEvent.Content.Payment(payment.patientId).trackUndoDelete()
        }
        return true
    }
}
