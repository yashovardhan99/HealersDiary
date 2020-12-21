package com.yashovardhan99.healersdiary.patients

import com.yashovardhan99.healersdiary.database.HealersDao
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientDetailRepository @Inject constructor(private val healersDao: HealersDao) {
    suspend fun getPatient(patientId: Long) = healersDao.getPatient(patientId)
    fun getHealings(patientId: Long, startDate: Date) = healersDao.getRecentHealings(patientId, startDate)
    fun getPayments(patientId: Long, startDate: Date) = healersDao.getRecentPayments(patientId, startDate)
}