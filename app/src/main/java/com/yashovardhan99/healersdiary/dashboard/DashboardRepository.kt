package com.yashovardhan99.healersdiary.dashboard

import com.yashovardhan99.healersdiary.database.HealersDao
import com.yashovardhan99.healersdiary.database.Healing
import com.yashovardhan99.healersdiary.database.Payment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.*
import javax.inject.Inject

class DashboardRepository @Inject constructor(private val healersDao: HealersDao) {
    val patients = healersDao.getAllPatients().distinctUntilChanged()
    fun getHealingsStarting(startDate: Date): Flow<List<Healing>> {
        return healersDao.getAllHealings(startDate)
    }

    fun getPaymentsStarting(startDate: Date): Flow<List<Payment>> {
        return healersDao.getAllPayments(startDate)
    }
}