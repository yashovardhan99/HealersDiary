package com.yashovardhan99.healersdiary.database

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
abstract class HealersDao {

    @Query("SELECT * FROM patients")
    abstract fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE id=:patientId")
    abstract suspend fun getPatient(patientId: Long): Patient?

    @Query("SELECT * FROM healings WHERE time>=:startDate")
    abstract fun getAllHealings(startDate: Date): Flow<List<Healing>>

    @Query("SELECT * FROM payments WHERE time>=:startDate")
    abstract fun getAllPayments(startDate: Date): Flow<List<Payment>>

    @Query("SELECT * FROM healings WHERE patient_id == :patientId ORDER BY time DESC")
    abstract fun getAllHealings(patientId: Long): PagingSource<Int, Healing>

    @Query("SELECT * FROM payments WHERE patient_id == :patientId ORDER BY time DESC")
    abstract fun getAllPayments(patientId: Long): PagingSource<Int, Payment>

    @Update(entity = Patient::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updatePatient(patient: Patient)

    @Insert(entity = Patient::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPatient(patient: Patient): Long

    @Insert(entity = Healing::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHealing(healing: Healing): Long

    @Transaction
    open suspend fun newHealing(healing: Healing): Long {
        val patient = getPatient(healing.patientId)
                ?: throw IllegalArgumentException("Invalid patient id")
        patient.due = patient.due + healing.charge
        patient.lastModified = maxOf(patient.lastModified, healing.time)
        updatePatient(patient)
        return insertHealing(healing)
    }

    @Transaction
    open suspend fun newPayment(payment: Payment): Long {
        val patient = getPatient(payment.patientId)
                ?: throw IllegalArgumentException("Invalid patient id")
        patient.due = patient.due - payment.amount
        patient.lastModified = maxOf(patient.lastModified, payment.time)
        updatePatient(patient)
        return insertPayment(payment)
    }

    @Insert(entity = Payment::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPayment(payment: Payment): Long
}