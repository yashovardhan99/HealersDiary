package com.yashovardhan99.healersdiary.patients

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashovardhan99.healersdiary.create.CreateRepository
import com.yashovardhan99.healersdiary.database.Patient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.util.*

class NewPatientViewModel @ViewModelInject constructor(private val repository: CreateRepository) : ViewModel() {
    private val _result = MutableStateFlow(-1L)
    val result: StateFlow<Long> = _result
    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error
    fun createPatient(name: String, charge: String, due: String, notes: String) {
        if (name.isBlank()) {
            _error.value = true
            return
        }
        try {
            _error.value = false
            val chargeInLong = if (charge.isBlank()) 0L else BigDecimal(charge).movePointRight(2).longValueExact()
            val dueInLong = if (due.isBlank()) 0L else BigDecimal(due).movePointRight(2).longValueExact()
            val patient = Patient(0, name, chargeInLong, dueInLong, notes, Date(), Date())
            viewModelScope.launch {
                val pid = repository.insertNewPatient(patient)
                Timber.d("New patient inserted; pid = $pid")
                _result.emit(pid)
            }
        } catch (e: NumberFormatException) {
            Timber.e(e, "Invalid amount")
            _error.value = true
        } catch (e: Exception) {
            Timber.e(e, "Error creating payment")
            _error.value = true
        }
    }

    fun resetError() {
        _error.value = false
    }

}