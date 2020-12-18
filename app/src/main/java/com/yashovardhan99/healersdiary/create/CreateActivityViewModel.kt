package com.yashovardhan99.healersdiary.create

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.yashovardhan99.healersdiary.dashboard.DashboardRepository
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.utils.setToStartOfDay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.*

class CreateActivityViewModel @ViewModelInject constructor(
        private val repository: DashboardRepository
) : ViewModel() {
    private val today = Calendar.getInstance().apply { setToStartOfDay() }.time
    private val healings = repository.getHealingsStarting(today)
    private val patientsFlow = repository.patients
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
}