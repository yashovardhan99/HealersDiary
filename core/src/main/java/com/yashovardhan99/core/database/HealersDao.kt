package com.yashovardhan99.core.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yashovardhan99.core.DangerousDatabase
import com.yashovardhan99.core.InternalDatabase
import com.yashovardhan99.core.toLocalDateTime
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

/**
 * Abstract dao implemented by the database.
 * Do not use directly. Use a repository to communicate.
 * @see HealersDatabase
 * @see DatabaseModule
 */
@OptIn(InternalDatabase::class)
@Dao
abstract class HealersDao {

    /**
     * Get all patients
     * @return a flow of the list of all patients
     */
    @Query("SELECT * FROM patients")
    abstract fun getAllPatients(): Flow<List<Patient>>

    /**
     * Get all healings
     * Use only when all healings are needed together
     * @return a list of all healings
     */
    @Query("SELECT * FROM healings")
    abstract suspend fun getAllHealings(): List<Healing>

    /**
     * Get all payments
     * Use only when all payments are needed together
     * @return a list of all payments
     */
    @Query("SELECT * FROM payments")
    abstract suspend fun getAllPayments(): List<Payment>

    /**
     * Get a particular patient
     * @param patientId The id of the patient to look
     * @return The patient, if found. Null, otherwise
     */
    @Query("SELECT * FROM patients WHERE id=:patientId")
    abstract suspend fun getPatient(patientId: Long): Patient?

    /**
     * Get a particular healing
     * @param healingId The id of the healing to find
     * @return The healing, if found. null otherwise
     */
    @Query("SELECT * FROM healings WHERE id=:healingId")
    abstract suspend fun getHealing(healingId: Long): Healing?

    /**
     * Get a particular payment
     * @param paymentId The id of the payment to find
     * @return The payment, if found. null otherwise
     */
    @Query("SELECT * FROM payments WHERE id=:paymentId")
    abstract suspend fun getPayment(paymentId: Long): Payment?

    /**
     * Get all healings starting after a particular date
     * @param startDate The date after which we want all the healings
     * @return A flow of the list of all healings found
     */
    @Query("SELECT * FROM healings WHERE time>=:startDate")
    abstract fun getAllHealings(startDate: LocalDateTime): Flow<List<Healing>>

    /**
     * Get all payments starting after a particular date
     * @param startDate The date after which we want all the payments
     * @return A flow of the list of all payments found
     */
    @Query("SELECT * FROM payments WHERE time>=:startDate")
    abstract fun getAllPayments(startDate: LocalDateTime): Flow<List<Payment>>

    /**
     * Get all healings for a particular patient
     * @param patientId The patient for which we need to find the healings
     * @return A paging source of all the healings found
     */
    @Query("SELECT * FROM healings WHERE patient_id = :patientId ORDER BY time DESC")
    abstract fun getAllHealings(patientId: Long): PagingSource<Int, Healing>

    /**
     * Get all payments for a particular patient
     * @param patientId The patient for which we need to find the payments
     * @return A paging source of all the payments found
     */
    @Query("SELECT * FROM payments WHERE patient_id = :patientId ORDER BY time DESC")
    abstract fun getAllPayments(patientId: Long): PagingSource<Int, Payment>

    /**
     * Update a patient record
     * @param patient the patient data to be updated
     */
    @Update(entity = Patient::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updatePatient(patient: Patient)

    /**
     * Update a healing record. Do not use directly.
     *
     * Use [updateHealing] instead.
     * @param healing The healing to update
     */
    @Update(entity = Healing::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun internalUpdateHealing(healing: Healing)

    /**
     * Update a payment record. Do not use directly.
     *
     * Use [updatePayment] instead.
     * @param payment The payment to update
     */
    @Update(entity = Payment::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun internalUpdatePayment(payment: Payment)

    /**
     * Update a healing record.
     *
     * This transaction also takes care of updating the patient record.
     * @param oldHealing The healing before the update
     * @param newHealing The updated healing. [Healing.id] and [Healing.patientId] must be same as [oldHealing]
     */
    @Transaction
    open suspend fun updateHealing(oldHealing: Healing, newHealing: Healing) {
        val patient = getPatient(oldHealing.patientId)
        val difference = newHealing.charge - oldHealing.charge
        val updatedPatient = patient?.copy(due = patient.due + difference)
        if (updatedPatient != null) {
            updatePatient(updatedPatient)
        }
        internalUpdateHealing(newHealing)
    }

    /**
     * Update a payment record.
     *
     * This transaction also takes care of updating the patient record.
     * @param oldPayment The Payment before the update
     * @param newPayment The updated payment. [Payment.id] and [Payment.patientId] must be same as [oldPayment]
     */
    @Transaction
    open suspend fun updatePayment(oldPayment: Payment, newPayment: Payment) {
        val patient = getPatient(oldPayment.patientId)
        val difference = oldPayment.amount - newPayment.amount
        val updatedPatient = patient?.copy(due = patient.due + difference)
        if (updatedPatient != null) {
            updatePatient(updatedPatient)
        }
        internalUpdatePayment(newPayment)
    }

    /**
     * Add new patient.
     * @param patient The patient to be added
     * @return The id of the patient added
     */
    @Insert(entity = Patient::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPatient(patient: Patient): Long

    /**
     * Add a new healing. Should not be used directly. Use newHealing instead
     * @param healing The healing to add
     * @return The id of the healing added
     * @see newHealing
     */
    @Insert(entity = Healing::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHealing(healing: Healing): Long

    /**
     * Database transaction to add a new healing and update patient details
     * @param healing The healing to be added
     * @return The id of the healing added
     */
    @Transaction
    open suspend fun newHealing(healing: Healing): Long {
        val patient = getPatient(healing.patientId)
            ?: throw IllegalArgumentException("Invalid patient id")
        updatePatient(
            patient.copy(
                due = patient.due + healing.charge,
                lastModified = maxOf(patient.lastModified, healing.time)
            )
        )
        return insertHealing(healing)
    }

    /**
     * Database transaction to add a new payment and update patient details
     * @param payment The payment to be added
     * @return The id of the payment added
     */
    @Transaction
    open suspend fun newPayment(payment: Payment): Long {
        val patient = getPatient(payment.patientId)
            ?: throw IllegalArgumentException("Invalid patient id")
        updatePatient(
            patient.copy(
                due = patient.due - payment.amount,
                lastModified = maxOf(patient.lastModified, payment.time.toLocalDateTime())
            )
        )
        return insertPayment(payment)
    }

    /**
     * Add a new payment. Should not be used directly. Use newPayment instead
     * @param payment The payment to add
     * @return The id of the payment added
     * @see newPayment
     */
    @Insert(entity = Payment::class, onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPayment(payment: Payment): Long

    /**
     * Delete a patient row. Should not be used directly. Use deletePatientData instead
     * @param patient The patient to delete
     */
    @InternalDatabase
    @Delete(entity = Patient::class)
    abstract suspend fun deletePatient(patient: Patient)

    /**
     * Delete all data associated with a patient
     * @param patient The patient whose data is to be deleted
     */
    @Transaction
    open suspend fun deletePatientData(patient: Patient) {
        deleteHealings(patient.id)
        deletePayments(patient.id)
        deletePatient(patient)
    }

    /**
     * Delete a single healing. Internal use only
     * @param healing The healing to delete
     */
    @InternalDatabase
    @Delete(entity = Healing::class)
    abstract suspend fun deleteHealingInternal(healing: Healing)

    /**
     * Delete a single healing and update the patient.
     * @param healing The healing to delete.
     */
    @Transaction
    open suspend fun deleteHealing(healing: Healing) {
        val patient = getPatient(healing.patientId)
        if (patient != null) {
            val updatedDue = patient.due - healing.charge
            updatePatient(patient.copy(due = updatedDue))
        }
        deleteHealingInternal(healing)
    }

    /**
     * Delete a single payment
     * @param payment The payment to delete
     */
    @InternalDatabase
    @Delete(entity = Payment::class)
    abstract suspend fun deletePaymentInternal(payment: Payment)

    /**
     * Delete a single payment and update the patient.
     * @param payment The payment to delete.
     */
    @Transaction
    open suspend fun deletePayment(payment: Payment) {
        val patient = getPatient(payment.patientId)
        if (patient != null) {
            val updatedDue = patient.due + payment.amount
            updatePatient(patient.copy(due = updatedDue))
        }
        deletePaymentInternal(payment)
    }

    /**
     * Get all activities.
     * @return A paging source with all activities sorted by time
     */
    @Query("SELECT * FROM activity")
    abstract fun getAllActivities(): PagingSource<Int, Activity>

    /**
     * Get all activities for a specific patient
     * @param patientId The id of the patient
     * @return A paging source with all activities sorted by time
     */
    @Query("SELECT * FROM activity  WHERE patient_id=:patientId")
    abstract fun getActivities(patientId: Long): PagingSource<Int, Activity>

    /**
     * Get all activities from a specific date
     * @param startDate The date after which we want all activities
     * @return The queried activities sorted by time
     */
    @Query("SELECT * FROM activity  WHERE time>=:startDate")
    abstract fun getActivities(startDate: LocalDateTime): Flow<List<Activity>>

    /**
     * Get count of all healings done between the specified times
     * @param startDate The time after which we want to count healings (inclusive)
     * @param endDate The time before which we want to count healings (inclusive)
     * @return The number of healings between the specified time period
     */
    @Query("SELECT COUNT(*) FROM healings  WHERE time BETWEEN :startDate AND :endDate")
    abstract fun getHealingCountBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<Int>

    /**
     * Get total charges of all healings done between the specified times
     * @param startDate The time after which we want to add healings (inclusive)
     * @param endDate The time before which we want to add healings (inclusive)
     * @return The total sum of charges of all healings between the specified time period
     */
    @Query("SELECT SUM(charge) FROM healings  WHERE time BETWEEN :startDate AND :endDate")
    abstract fun getHealingAmountBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<Long?>

    /**
     * Get count of all healings done between the specified times for a specific patient
     * @param startDate The time after which we want to count healings (inclusive)
     * @param endDate The time before which we want to count healings (inclusive)
     * @param patientId The patient whose healings are required
     * @return The number of healings between the specified time period
     */
    @Query(
        "SELECT COUNT(*) FROM healings  WHERE time BETWEEN :startDate AND :endDate" +
                " AND patient_id = :patientId"
    )
    abstract fun getHealingCountBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        patientId: Long
    ): Flow<Int>

    /**
     * Get total charges of all healings done between the specified times for a specific patient
     * @param startDate The time after which we want to add healings (inclusive)
     * @param endDate The time before which we want to add healings (inclusive)
     * @param patientId The patient whose healings are queried
     * @return The total sum of charges of all healings between the specified time period
     */
    @Query(
        "SELECT SUM(charge) FROM healings  WHERE time BETWEEN :startDate AND :endDate " +
                "AND patient_id = :patientId"
    )
    abstract fun getHealingAmountBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        patientId: Long
    ): Flow<Long?>

    /**
     * Delete all healings for a particular patient. Should not be used directly
     * @param patientId The patient whose healings are to be deleted.
     * @see deletePatientData
     */
    @InternalDatabase
    @Query("DELETE FROM healings WHERE patient_id=:patientId")
    abstract suspend fun deleteHealings(patientId: Long)

    /**
     * Delete all payments for a particular patient. Should not be used directly
     * @param patientId The patient whose payments are to be deleted.
     * @see deletePatientData
     */
    @InternalDatabase
    @Query("DELETE FROM payments WHERE patient_id=:patientId")
    abstract suspend fun deletePayments(patientId: Long)

    /**
     * Dangerous operation. Delete all patients
     */
    @DangerousDatabase
    @Query("DELETE FROM patients")
    abstract suspend fun deleteAllPatients()

    /**
     * Dangerous operation. Delete all healings
     */
    @DangerousDatabase
    @Query("DELETE FROM healings")
    abstract suspend fun deleteAllHealings()

    /**
     * Dangerous operation. Delete all payments
     */
    @DangerousDatabase
    @Query("DELETE FROM payments")
    abstract suspend fun deleteAllPayments()
}
