package com.yashovardhan99.healersdiary.dashboard

import com.yashovardhan99.healersdiary.database.HealersDao
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class DashboardRepository @Inject constructor(private val healersDao: HealersDao) {
    val patients = healersDao.getAllPatients().distinctUntilChanged()
}