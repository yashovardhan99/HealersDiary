package com.yashovardhan99.healersdiary.patients

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yashovardhan99.healersdiary.database.HealersDao
import com.yashovardhan99.healersdiary.database.Healing
import com.yashovardhan99.healersdiary.database.Payment
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientDetailRepository @Inject constructor(private val healersDao: HealersDao) {
    suspend fun getPatient(patientId: Long) = healersDao.getPatient(patientId)
    fun getHealings(patientId: Long, startDate: Date) = healersDao.getRecentHealings(patientId, startDate)
    fun getPayments(patientId: Long, startDate: Date) = healersDao.getRecentPayments(patientId, startDate)
    fun getAllHealings(patientId: Long): Flow<PagingData<Healing>> {
        return Pager(
                config = PagingConfig(pageSize = 20)) {
            healersDao.getAllHealings(patientId)
        }.flow
    }

    fun getAllPayments(patientId: Long): Flow<PagingData<Payment>> {
        return Pager(
                config = PagingConfig(pageSize = 20)) {
            healersDao.getAllPayments(patientId)
        }.flow
    }

    suspend fun deleteHealing(healing: Healing) {
        healersDao.deleteHealing(healing)
    }

    suspend fun deletePayment(payment: Payment) {
        healersDao.deletePayment(payment)
    }
}