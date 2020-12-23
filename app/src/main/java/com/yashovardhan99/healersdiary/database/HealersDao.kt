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

    @Query("SELECT * FROM healings WHERE patient_id = :patientId ORDER BY time DESC")
    abstract fun getAllHealings(patientId: Long): PagingSource<Int, Healing>

    @Query("SELECT * FROM payments WHERE patient_id = :patientId ORDER BY time DESC")
    abstract fun getAllPayments(patientId: Long): PagingSource<Int, Payment>

    @Query("SELECT * FROM healings WHERE patient_id = :patientId AND time>=:startDate")
    abstract fun getRecentHealings(patientId: Long, startDate: Date): Flow<List<Healing>>

    @Query("SELECT * FROM payments WHERE patient_id = :patientId AND time>=:startDate")
    abstract fun getRecentPayments(patientId: Long, startDate: Date): Flow<List<Payment>>

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
        updatePatient(patient.copy(due = patient.due + healing.charge,
                lastModified = maxOf(patient.lastModified, healing.time)))
        return insertHealing(healing)
    }

    @Transaction
    open suspend fun newPayment(payment: Payment): Long {
        val patient = getPatient(payment.patientId)
                ?: throw IllegalArgumentException("Invalid patient id")
        updatePatient(patient.copy(due = patient.due - payment.amount,
                lastModified = maxOf(patient.lastModified, payment.time)))
        return insertPayment(payment)
    }

    @Insert(entity = Payment::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPayment(payment: Payment): Long

    @Delete(entity = Patient::class)
    abstract suspend fun deletePatient(patient: Patient)

    @Transaction
    open suspend fun deletePatientData(patient: Patient) {
        deleteHealings(patient.id)
        deletePayments(patient.id)
        deletePatient(patient)
    }

    @Delete(entity = Healing::class)
    abstract suspend fun deleteHealing(healing: Healing)

    @Delete(entity = Payment::class)
    abstract suspend fun deletePayment(payment: Payment)

    @Query("SELECT * FROM activity ORDER BY time")
    abstract fun getAllActivities(): PagingSource<Int, Activity>

    @Query("SELECT * FROM activity  WHERE patient_id=:patientId ORDER BY time")
    abstract fun getActivities(patientId: Long): PagingSource<Int, Activity>

    @Query("DELETE FROM healings WHERE patient_id=:patientId")
    abstract suspend fun deleteHealings(patientId: Long)

    @Query("DELETE FROM payments WHERE patient_id=:patientId")
    abstract suspend fun deletePayments(patientId: Long)
}