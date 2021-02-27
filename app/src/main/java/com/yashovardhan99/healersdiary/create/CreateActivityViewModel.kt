package com.yashovardhan99.healersdiary.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashovardhan99.healersdiary.dashboard.DashboardRepository
import com.yashovardhan99.core.database.ActivityType
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import com.yashovardhan99.healersdiary.utils.Request
import com.yashovardhan99.core.setToStartOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreateActivityViewModel @Inject constructor(
        private val dashboardRepository: DashboardRepository,
        private val createRepository: CreateRepository
) : ViewModel() {
    var selectedPatient: Patient? = null
        private set
    private val today = Calendar.getInstance().apply { setToStartOfDay() }.time
    private val healings = dashboardRepository.getHealingsStarting(today)
    private val patientsFlow = dashboardRepository.patients
    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatientFlow: StateFlow<Patient?> = _selectedPatient
    private val _activityCalendar = MutableStateFlow(Calendar.getInstance())
    val activityCalendar: StateFlow<Calendar> = _activityCalendar
    private val _selectedActivityType = MutableStateFlow<ActivityType?>(null)
    val selectedActivityType: StateFlow<ActivityType?> = _selectedActivityType
    val patients = healings.combine(patientsFlow) { healings, patients ->
        val patientsMap = patients.associateBy { it.id }
        val patientWithHealings = healings.groupBy {
            patientsMap[it.patientId] ?: Patient.MissingPatient
        }
        patients.map { patient ->
            val healingsToday = patientWithHealings[patient]?.count { it.time >= today } ?: 0
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

    fun setActivityCalendar(calendar: Calendar) {
        _activityCalendar.value = calendar
    }

    fun selectPatient(patient: Patient?) {
        if (patient != null) selectedPatient = patient
        viewModelScope.launch {
            _selectedPatient.emit(patient)
        }
    }

    fun selectPatient(pid: Long, activityType: ActivityType? = null) {
        viewModelScope.launch {
            val patient = dashboardRepository.getPatient(pid)
            _selectedActivityType.emit(activityType)
            if (patient != null) selectPatient(patient)
        }
    }

    fun newPatient() {
        _result.value = Request.NewPatient
    }

    fun createHealing(charge: String, notes: String, pid: Long) {
        try {
            _error.value = false
            val chargeInLong = if (charge.isBlank()) 0 else BigDecimal(charge).movePointRight(2).longValueExact()
            val healing = Healing(0, _activityCalendar.value.time, chargeInLong, notes, pid)
            viewModelScope.launch {
                createRepository.insertNewHealing(healing)
                Timber.d("Inserted new Healing!")
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
            val amountInLong = if (amount.isBlank()) 0L else BigDecimal(amount).movePointRight(2).longValueExact()
            val payment = Payment(0, _activityCalendar.value.time, amountInLong, notes, pid)
            viewModelScope.launch {
                createRepository.insertNewPayment(payment)
                Timber.d("Inserted new Payment!")
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