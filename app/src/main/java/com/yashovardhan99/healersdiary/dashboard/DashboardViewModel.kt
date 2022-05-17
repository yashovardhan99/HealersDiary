package com.yashovardhan99.healersdiary.dashboard

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.toHealing
import com.yashovardhan99.core.database.toPayment
import com.yashovardhan99.core.getStartOfLastMonth
import com.yashovardhan99.core.getStartOfMonth
import com.yashovardhan99.core.utils.*
import com.yashovardhan99.core.utils.ActivityParent.Activity.Companion.getSeparator
import com.yashovardhan99.core.utils.Stat.Companion.earnedLastMonth
import com.yashovardhan99.core.utils.Stat.Companion.earnedThisMonth
import com.yashovardhan99.core.utils.Stat.Companion.healingsThisMonth
import com.yashovardhan99.core.utils.Stat.Companion.healingsToday
import com.yashovardhan99.healersdiary.ShortcutData
import com.yashovardhan99.healersdiary.create.CreateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

/**
 * Viewmodel shared between all top level destinations and some inner destinations
 * @param repository The dashboard repository consumed
 * @see MainActivity
 * @see DashboardRepository
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DashboardRepository,
    private val createRepository: CreateRepository
) : ViewModel() {

    private val todayDate = LocalDate.now()
    private val thisMonthDate = todayDate.getStartOfMonth()
    private val lastMonthDate = todayDate.getStartOfLastMonth()

    // current selected patient (for inner pages)
    private var currentPatientId = -1L

    private val _requests = MutableStateFlow<Request?>(null)
    val requests: StateFlow<Request?> = _requests
    private val _shortcuts = MutableSharedFlow<ShortcutData>(2)
    val shortcuts: SharedFlow<ShortcutData> = _shortcuts


    private fun getPatientsMap(): Flow<Map<Long, Patient>> = repository.getPatients().map { list ->
        list.associateBy(Patient::id)
    }

    /**
     * Patients list sorted by last modified
     * Includes - associated healings data
     */
    fun getPatients() = repository.getHealingsStarting(lastMonthDate)
        // healing flow starting last month (eg. If it's february, include all healings starting January)
        .combine(getPatientsMap()) { healings, patientsMap ->
            Timber.d(patientsMap.toString())
            Timber.d("Healings 1 = $healings")
            val patientWithHealings = healings.groupBy {
                patientsMap[it.patientId] ?: Patient.MissingPatient
            }
            // setting no. of healings today and this month for patients list page
            patientsMap.values.map { patient ->
                val today =
                    patientWithHealings[patient]?.count { it.time >= todayDate.atStartOfDay() } ?: 0
                val thisMonth =
                    patientWithHealings[patient]?.count { it.time >= thisMonthDate.atStartOfDay() }
                        ?: 0
                patient.copy(healingsToday = today, healingsThisMonth = thisMonth)
            }.sortedByDescending { it.lastModified }
        }

    fun getActivities() = repository.getAllActivities()
        .cachedIn(viewModelScope)
        .combine(getPatientsMap()) { pagingData, patientsMap ->
            pagingData.map { it.toUiActivity(patientsMap) }
                .insertSeparators(generator = ::getSeparator)
        }.onEmpty { emit(PagingData.empty()) }
        .cachedIn(viewModelScope)

    fun getStats(): Flow<List<Stat>> = combineTransform(
        repository.getHealingCountBetween(todayDate, todayDate), //healings today
        repository.getHealingCountBetween(thisMonthDate, todayDate), //healings this month
        repository.getHealingAmountBetween(thisMonthDate, todayDate), //Earned this month
        // Earned last month
        repository.getHealingAmountBetween(lastMonthDate, thisMonthDate.minusDays(1))
    ) { todayCount, thisMonthCount, thisMonthAmount, lastMonthAmount ->
        val stats = listOf(
            healingsToday(todayCount),
            healingsThisMonth(thisMonthCount),
            earnedThisMonth(thisMonthAmount.toBigDecimal().movePointLeft(2)),
            earnedLastMonth(lastMonthAmount.toBigDecimal().movePointLeft(2))
        )
        emit(stats)
    }.onStart {
        val emptyStats = listOf(
            healingsToday(0),
            healingsThisMonth(0),
            earnedThisMonth(BigDecimal.ZERO),
            earnedLastMonth(BigDecimal.ZERO)
        )
        emit(emptyStats)
    }

    fun viewPatient(patientId: Long) {
        _requests.value = Request.ViewPatient(patientId)
    }

    fun newHealing(patientId: Long) {
        _requests.value = Request.NewHealing(patientId)
    }

    fun newPayment(patientId: Long) {
        _requests.value = Request.NewPayment(patientId)
    }

    fun resetRequest() {
        _requests.value = null
    }

    fun addNewPatient() {
        _requests.value = Request.NewPatient
    }

    fun editPatient(patientId: Long) {
        _requests.value = Request.UpdatePatient(patientId)
    }

    fun setPatientId(patientId: Long) {
        currentPatientId = patientId
    }

    fun resetPatientId() {
        currentPatientId = -1L
    }

    fun getPatientId(): Long = currentPatientId
    fun editHealing(healing: HealingParent.Healing) {
        _requests.value = Request.UpdateHealing(healing.patientId, healing.id)
    }

    fun editPayment(payment: PaymentParent.Payment) {
        _requests.value = Request.UpdatePayment(payment.patientId, payment.id)
    }

    fun editActivity(activity: ActivityParent.Activity) {
        when (activity.type) {
            ActivityParent.Activity.Type.HEALING -> _requests.value =
                Request.UpdateHealing(activity.patient.id, activity.id)
            ActivityParent.Activity.Type.PATIENT -> _requests.value =
                Request.UpdatePatient(activity.patient.id)
            ActivityParent.Activity.Type.PAYMENT -> _requests.value =
                Request.UpdatePayment(activity.patient.id, activity.id)
        }
    }

    fun deleteActivity(activity: ActivityParent.Activity) {
        savedStateHandle["deleted_activity"] = null
        when (activity.type) {
            ActivityParent.Activity.Type.HEALING -> deleteHealing(activity)
            ActivityParent.Activity.Type.PAYMENT -> deletePayment(activity)
            ActivityParent.Activity.Type.PATIENT ->
                throw IllegalArgumentException("$activity cannot be of type ${ActivityParent.Activity.Type.PATIENT}")
        }
    }

    private fun deleteHealing(activity: ActivityParent.Activity) {
        if (activity.type != ActivityParent.Activity.Type.HEALING)
            throw IllegalArgumentException("$activity must be of type ${ActivityParent.Activity.Type.HEALING}")
        viewModelScope.launch {
            createRepository.getHealing(activity.id)?.let { healing ->
                savedStateHandle["deleted_healing"] = healing.toBundle()
                savedStateHandle["deleted_type"] = "healing"
                repository.deleteHealing(healing)
                AnalyticsEvent.Content.Healing(healing.patientId).trackDelete()
            }
        }
    }


    fun undoDeleteActivity(): Boolean {
        val type: String = savedStateHandle.remove("deleted_type") ?: return false
        return when (type) {
            "healing" -> undoDeleteHealing()
            "payment" -> undoDeletePayment()
            else -> false
        }
    }

    private fun undoDeleteHealing(): Boolean {
        val healing = savedStateHandle.remove<Bundle>("deleted_healing")?.toHealing()
            ?: return false
        viewModelScope.launch {
            createRepository.insertNewHealing(healing)
            AnalyticsEvent.Content.Healing(healing.patientId).trackUndoDelete()
        }
        return true
    }

    private fun deletePayment(activity: ActivityParent.Activity) {
        if (activity.type != ActivityParent.Activity.Type.PAYMENT)
            throw IllegalArgumentException("$activity must be of type ${ActivityParent.Activity.Type.PAYMENT}")
        viewModelScope.launch {
            createRepository.getPayment(activity.id)?.let { payment ->
                savedStateHandle["deleted_payment"] = payment.toBundle()
                savedStateHandle["deleted_type"] = "payment"
                repository.deletePayment(payment)
                AnalyticsEvent.Content.Payment(payment.patientId).trackDelete()
            }
        }
    }

    private fun undoDeletePayment(): Boolean {
        val payment = savedStateHandle.remove<Bundle>("deleted_payment")?.toPayment()
            ?: return false
        viewModelScope.launch {
            createRepository.insertNewPayment(payment)
            AnalyticsEvent.Content.Payment(payment.patientId).trackUndoDelete()
        }
        return true
    }

    fun requestShortcuts(maxShortcutCount: Int) {
        Timber.d("Shortcut count = $maxShortcutCount")
        viewModelScope.launch {
            val patients = repository.getPatients().first().sortedByDescending { it.lastModified }
                .take(maxShortcutCount - 2).withIndex()
            Timber.d("Patients being taken for shortcuts = $patients")
            for (patient in patients) {
                _shortcuts.emit(
                    ShortcutData(
                        patient.value.id,
                        patient.value.name,
                        patient.index
                    )
                )
            }
        }
    }

    init {
        Timber.d(
            """
                DATES: Today = $todayDate
                This month = $thisMonthDate
                Last month = $lastMonthDate
            """.trimIndent()
        )
    }
}
