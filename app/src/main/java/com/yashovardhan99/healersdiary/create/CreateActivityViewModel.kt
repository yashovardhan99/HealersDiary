package com.yashovardhan99.healersdiary.create

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashovardhan99.healersdiary.dashboard.DashboardRepository
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.utils.setToStartOfDay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class CreateActivityViewModel @ViewModelInject constructor(
        private val repository: DashboardRepository
) : ViewModel() {
    var selectedPatient: Patient? = null
        private set
    private val today = Calendar.getInstance().apply { setToStartOfDay() }.time
    private val healings = repository.getHealingsStarting(today)
    private val patientsFlow = repository.patients
    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatientFlow: StateFlow<Patient?> = _selectedPatient
    private val _activityCalendar = MutableStateFlow(Calendar.getInstance())
    val activityCalendar: StateFlow<Calendar> = _activityCalendar
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

    fun setActivityCalendar(calendar: Calendar) {
        _activityCalendar.value = calendar
    }

    fun selectPatient(patient: Patient?) {
        if (patient != null) selectedPatient = patient
        viewModelScope.launch {
            _selectedPatient.emit(patient)
        }
    }

    fun selectPatient(pid: Long) {
        viewModelScope.launch {
            val patient = repository.getPatient(pid)
            if (patient != null) selectPatient(patient)
        }
    }
}