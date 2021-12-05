package com.yashovardhan99.healersdiary.patients

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.utils.Request
import com.yashovardhan99.healersdiary.create.CreateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel used by [NewPatientActivity].
 *
 * Responsible for maintaining state, getting patient data (for edits), saving and deleting patient and validating input.
 * @see NewPatientActivity
 * @see CreateRepository
 * @property repository The creation repository
 */
@HiltViewModel
class NewPatientViewModel @Inject constructor(private val repository: CreateRepository) :
    ViewModel() {
    // Sends the result of save/delete to the activity
    private val _result = MutableStateFlow<Result>(Result.Unset)
    val result: StateFlow<Result> = _result

    // Sends data about any error
    // TODO: 25/2/21 Refactor this to sealed class, channel as flow
    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error

    // Not used TODO: Remove/refactor this
    private val _request = MutableStateFlow<Request?>(null)
    val request: StateFlow<Request?> = _request

    // Data about the current patient being edited (null => new patient)
    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient

    /**
     * Called to validate and save the data.
     *
     * Any errors are sent via [error]. On successful saving, [result] is set to [Result.Success]
     */
    fun save(name: String, charge: String, due: String, notes: String) {
        if (name.isBlank()) {
            _error.value = true
            return
        }
        try {
            _error.value = false
            val chargeInLong =
                if (charge.isBlank()) 0L else BigDecimal(charge).movePointRight(2).longValueExact()
            val dueInLong =
                if (due.isBlank()) 0L else BigDecimal(due).movePointRight(2).longValueExact()
            val patient = _patient.value
            // If patient exists (editing), update it ELSE create a new patient
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

    /**
     * Update the patient details in the database and send [Result.Success]
     */
    private fun updatePatient(
        patient: Patient,
        name: String,
        chargeInLong: Long,
        dueInLong: Long,
        notes: String
    ) {
        val updatedPatient = patient.copy(
            name = name, charge = chargeInLong,
            due = dueInLong, notes = notes, lastModified = LocalDateTime.now()
        )
        viewModelScope.launch {
            repository.updatePatient(updatedPatient)
            Timber.d("Patient updated; pid = ${patient.id}")
            AnalyticsEvent.Content.Patient(patient.id).trackEdit()
            _result.emit(Result.Success(patient.id))
        }
    }

    /**
     * Create a new patient in the database and send [Result.Success]
     */
    private fun createNew(name: String, chargeInLong: Long, dueInLong: Long, notes: String) {
        val dateTime = LocalDateTime.now()
        val patient =
            Patient(0, name, chargeInLong, dueInLong, notes, dateTime, dateTime)
        viewModelScope.launch {
            val pid = repository.insertNewPatient(patient)
            Timber.d("New patient inserted; pid = $pid")
            AnalyticsEvent.Content.Patient(pid).trackCreate()
            _result.emit(Result.Success(pid))
        }
    }

    /**
     * Delete the patient and all related data from the database.
     *
     * Sets [result] as [Result.Deleted]
     */
    fun deletePatient() {
        val patient = _patient.value ?: return
        viewModelScope.launch {
            repository.deletePatient(patient)
            AnalyticsEvent.Content.Patient(patient.id).trackDelete()
            _result.emit(Result.Deleted)
        }
    }

    /**
     * Receive the uri from the activity. Used to set the patient details for editing
     */
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

    /**
     * Called after receiving an [error] to reset its value
     */
    fun resetError() {
        _error.value = false
    }

    companion object {
        /**
         * Class used to represent the result of the operations performed here.
         */
        sealed class Result {
            data class Success(val patientId: Long) : Result()
            object Deleted : Result()
            object Unset : Result()
        }
    }
}