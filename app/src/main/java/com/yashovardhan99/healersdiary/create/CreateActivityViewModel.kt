package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.database.ActivityType
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import com.yashovardhan99.core.database.toPatient
import com.yashovardhan99.core.utils.Request
import com.yashovardhan99.healersdiary.dashboard.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class CreateActivityViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val createRepository: CreateRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val today = LocalDate.now()
    private val healings = dashboardRepository.getHealingsStarting(today)
    private val patientsFlow = dashboardRepository.patients
    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatientFlow: StateFlow<Patient?> = _selectedPatient
    private val _activityTime = MutableStateFlow(LocalDateTime.now())
    val activityTime: StateFlow<LocalDateTime> = _activityTime
    private val _selectedActivityType = MutableStateFlow<ActivityType?>(null)
    val selectedActivityType: StateFlow<ActivityType?> = _selectedActivityType
    val patients = healings.combine(patientsFlow) { healings, patients ->
        val patientsMap = patients.associateBy { it.id }
        val patientWithHealings = healings.groupBy {
            patientsMap[it.patientId] ?: Patient.MissingPatient
        }
        patients.map { patient ->
            val healingsToday =
                patientWithHealings[patient]?.count { it.time >= today.atStartOfDay() } ?: 0
            patient.copy(healingsToday = healingsToday)
        }
    }.distinctUntilChanged().conflate()

    private val _result = MutableStateFlow<Request?>(null)
    val result: StateFlow<Request?> = _result

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error

    fun resetError() {
        _error.value = false
    }

    fun setActivityTime(time: LocalDateTime) {
        _activityTime.value = time
    }

    fun selectPatient(patient: Patient?) {
        viewModelScope.launch {
            _selectedPatient.emit(patient)
        }
    }

    private suspend fun getPatientDetails(patientId: Long): Patient? {
        val savedPatient: Patient? =
            savedStateHandle.get<Bundle?>("patient-$patientId")?.toPatient()
        if (savedPatient != null) return savedPatient
        return dashboardRepository.getPatient(patientId).also {
            savedStateHandle["patient-$patientId"] = it?.toBundle()
        }
    }

    fun selectPatient(pid: Long, activityType: ActivityType? = null) {
        viewModelScope.launch {
            val patient = getPatientDetails(pid)
            _selectedActivityType.emit(activityType)
            if (patient != null) selectPatient(patient)
        }
    }

    private var _healingEdit = MutableStateFlow<Healing?>(null)
    private var _paymentEdit = MutableStateFlow<Payment?>(null)
    fun getHealing() = _healingEdit.asStateFlow()
    fun getPayment() = _paymentEdit.asStateFlow()

    fun requestEdit(pid: Long, activityId: Long, activityType: ActivityType) {
        viewModelScope.launch {
            selectPatient(pid, activityType)
            when (activityType) {
                ActivityType.HEALING -> {
                    val healing = createRepository.getHealing(activityId)
                    if (healing != null)
                        setActivityTime(healing.time)
                    _healingEdit.value = healing
                }
                ActivityType.PAYMENT -> {
                    val payment = createRepository.getPayment(activityId)
                    if (payment != null) setActivityTime(payment.time)
                    _paymentEdit.value = payment
                }
                else -> throw IllegalArgumentException("activityType must be either ${ActivityType.HEALING} or ${ActivityType.PATIENT}")
            }
        }
    }

    fun newPatient() {
        _result.value = Request.NewPatient
    }

    fun createHealing(charge: String, notes: String, pid: Long) {
        try {
            _error.value = false
            val chargeInLong =
                if (charge.isBlank()) 0 else BigDecimal(charge).movePointRight(2).longValueExact()
            val current = getHealing().value
            val healing = if (current != null) Healing(
                current.id,
                _activityTime.value,
                chargeInLong,
                notes,
                pid
            )
            else Healing(0, _activityTime.value, chargeInLong, notes, pid)
            viewModelScope.launch {
                if (current != null) {
                    createRepository.updateHealing(current, healing)
                    Timber.d("Updated healing ${healing.id}")
                    AnalyticsEvent.Content.Healing(healing.patientId).trackEdit()
                } else {
                    createRepository.insertNewHealing(healing)
                    Timber.d("Inserted new Healing!")
                    AnalyticsEvent.Content.Healing(healing.patientId).trackCreate()
                }
                _result.emit(Request.ViewPatient(pid))
            }
        } catch (e: NumberFormatException) {
            _error.value = true
            Timber.e(e, "Invalid charge")
        } catch (e: Exception) {
            _error.value = true
            Timber.e(e, "Error creating healing")
        }
    }

    fun createPayment(amount: String, notes: String, pid: Long) {
        try {
            _error.value = false
            val current = getPayment().value
            val amountInLong =
                if (amount.isBlank()) 0L else BigDecimal(amount).movePointRight(2).longValueExact()
            val payment = current?.copy(
                time = _activityTime.value,
                amount = amountInLong,
                notes = notes
            )
                ?: Payment(0, _activityTime.value, amountInLong, notes, pid)
            viewModelScope.launch {
                if (current != null) {
                    createRepository.updatePayment(current, payment)
                    Timber.d("Updated payment: ${payment.id}")
                    AnalyticsEvent.Content.Payment(payment.patientId).trackEdit()
                } else {
                    createRepository.insertNewPayment(payment)
                    Timber.d("Inserted new Payment!")
                    AnalyticsEvent.Content.Payment(payment.patientId).trackCreate()
                }
                _result.emit(Request.ViewPatient(pid))
            }
        } catch (e: NumberFormatException) {
            _error.value = true
            Timber.e(e, "Invalid amount")
        } catch (e: Exception) {
            _error.value = true
            Timber.e(e, "Error creating payment")
        }
    }

    fun resetActivityType() {
        viewModelScope.launch {
            _selectedActivityType.emit(null)
        }
    }
}

