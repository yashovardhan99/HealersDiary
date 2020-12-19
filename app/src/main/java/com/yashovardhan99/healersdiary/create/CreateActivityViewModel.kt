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
    private var lastSelected: Patient? = null
    private val today = Calendar.getInstance().apply { setToStartOfDay() }.time
    private val healings = repository.getHealingsStarting(today)
    private val patientsFlow = repository.patients
    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatient: StateFlow<Patient?> = _selectedPatient
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

    fun selectPatient(patient: Patient?) {
        if (patient != null) lastSelected = patient
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