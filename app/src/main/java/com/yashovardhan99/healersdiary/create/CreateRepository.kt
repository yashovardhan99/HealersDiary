package com.yashovardhan99.healersdiary.create

import com.yashovardhan99.core.database.HealersDao
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRepository @Inject constructor(private val healersDao: HealersDao) {
    suspend fun insertNewHealing(healing: Healing) = healersDao.newHealing(healing)
    suspend fun insertNewPayment(payment: Payment) = healersDao.newPayment(payment)
    suspend fun insertNewPatient(patient: Patient) = healersDao.insertPatient(patient)
    suspend fun getPatient(patientId: Long): Patient? = healersDao.getPatient(patientId)
    suspend fun updatePatient(updatedPatient: Patient) = healersDao.updatePatient(updatedPatient)
    suspend fun deletePatient(patient: Patient) = healersDao.deletePatientData(patient)
}