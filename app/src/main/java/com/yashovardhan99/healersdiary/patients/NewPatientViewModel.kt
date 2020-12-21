package com.yashovardhan99.healersdiary.patients

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashovardhan99.healersdiary.create.CreateRepository
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.utils.Request
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.util.*

class NewPatientViewModel @ViewModelInject constructor(private val repository: CreateRepository) : ViewModel() {
    private val _result = MutableStateFlow<Result>(Result.Unset)
    val result: StateFlow<Result> = _result
    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error
    private val _request = MutableStateFlow<Request?>(null)
    val request: StateFlow<Request?> = _request
    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient

    fun save(name: String, charge: String, due: String, notes: String) {
        if (name.isBlank()) {
            _error.value = true
            return
        }
        try {
            _error.value = false
            val chargeInLong = if (charge.isBlank()) 0L else BigDecimal(charge).movePointRight(2).longValueExact()
            val dueInLong = if (due.isBlank()) 0L else BigDecimal(due).movePointRight(2).longValueExact()
            val patient = _patient.value
            if (patient == null) createNew(name, chargeInLong, dueInLong, notes)
            else updatePatient(patient, name, chargeInLong, dueInLong, notes)
        } catch (e: NumberFormatException) {
            Timber.e(e, "Invalid amount")
            _error.value = true
        } catch (e: Exception) {
            Timber.e(e, "Error saving!")
            _error.value = true
        }
    }

    private fun updatePatient(patient: Patient, name: String, chargeInLong: Long, dueInLong: Long, notes: String) {
        val updatedPatient = patient.copy(name = name, charge = chargeInLong,
                due = dueInLong, notes = notes, lastModified = Date())
        viewModelScope.launch {
            repository.updatePatient(updatedPatient)
            Timber.d("Patient updated; pid = ${patient.id}")
            _result.emit(Result.Success(patient.id))
        }
    }

    private fun createNew(name: String, chargeInLong: Long, dueInLong: Long, notes: String) {
        val patient = Patient(0, name, chargeInLong, dueInLong, notes, Date(), Date())
        viewModelScope.launch {
            val pid = repository.insertNewPatient(patient)
            Timber.d("New patient inserted; pid = $pid")
            _result.emit(Result.Success(pid))
        }
    }

    fun deletePatient() {
        val patient = _patient.value ?: return
        viewModelScope.launch {
            repository.deletePatient(patient)
            _result.emit(Result.Deleted)
        }
    }

    fun setRequest(uri: Uri) {
        val req = Request.fromUri(uri)
        _request.value = req
        viewModelScope.launch {
            if (req is Request.UpdatePatient) {
                val patient = repository.getPatient(req.patientId)
                _patient.emit(patient)
            }
        }
    }

    fun resetError() {
        _error.value = false
    }

    companion object {
        sealed class Result {
            data class Success(val patientId: Long) : Result()
            object Deleted : Result()
            object Unset : Result()
        }
    }
}