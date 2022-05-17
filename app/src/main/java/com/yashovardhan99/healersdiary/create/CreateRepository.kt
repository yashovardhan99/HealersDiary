package com.yashovardhan99.healersdiary.create

import com.yashovardhan99.core.database.HealersDao
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import javax.inject.Inject

interface CreateRepository {
    suspend fun insertNewHealing(healing: Healing): Long
    suspend fun insertNewPayment(payment: Payment): Long
    suspend fun insertNewPatient(patient: Patient): Long
    suspend fun getPatient(patientId: Long): Patient?
    suspend fun updatePatient(updatedPatient: Patient)
    suspend fun deletePatient(patient: Patient)
    suspend fun getHealing(healingId: Long): Healing?
    suspend fun getPayment(paymentId: Long): Payment?
    suspend fun updateHealing(oldHealing: Healing, newHealing: Healing)
    suspend fun updatePayment(oldPayment: Payment, newPayment: Payment)
}

class CreateRepositoryImpl @Inject constructor(private val healersDao: HealersDao) :
    CreateRepository {
    override suspend fun insertNewHealing(healing: Healing) = healersDao.newHealing(healing)
    override suspend fun insertNewPayment(payment: Payment) = healersDao.newPayment(payment)
    override suspend fun insertNewPatient(patient: Patient) = healersDao.insertPatient(patient)
    override suspend fun getPatient(patientId: Long): Patient? = healersDao.getPatient(patientId)
    override suspend fun updatePatient(updatedPatient: Patient) =
        healersDao.updatePatient(updatedPatient)

    override suspend fun deletePatient(patient: Patient) = healersDao.deletePatientData(patient)
    override suspend fun getHealing(healingId: Long): Healing? = healersDao.getHealing(healingId)
    override suspend fun getPayment(paymentId: Long): Payment? = healersDao.getPayment(paymentId)
    override suspend fun updateHealing(oldHealing: Healing, newHealing: Healing) =
        healersDao.updateHealing(oldHealing, newHealing)

    override suspend fun updatePayment(oldPayment: Payment, newPayment: Payment) =
        healersDao.updatePayment(oldPayment, newPayment)
}