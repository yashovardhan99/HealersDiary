package com.yashovardhan99.healersdiary.dashboard

import com.yashovardhan99.core.database.Activity
import com.yashovardhan99.core.database.HealersDao
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import java.time.LocalDate
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Database repository for the database.
 * This repository does not modify the database
 * @param healersDao Dao for the database
 * @see DashboardViewModel
 * @see HealersDao
 */
@Singleton
class DashboardRepository @Inject constructor(private val healersDao: HealersDao) {
    /**
     * Flow of all the patients
     */
    val patients = healersDao.getAllPatients().distinctUntilChanged()

    /**
     * Get all healings done after startDate
     * @param startDate The date after which all healings are needed
     * @return flow of a list of all healings starting startDate
     */
    fun getHealingsStarting(startDate: Date): Flow<List<Healing>> {
        return healersDao.getAllHealings(startDate)
    }

    /**
     * Get all payments done after startDate
     * @param startDate The date after which all payments are needed
     * @return flow of a list of all payments starting startDate
     */
    fun getPaymentsStarting(startDate: Date): Flow<List<Payment>> {
        return healersDao.getAllPayments(startDate)
    }

    /**
     * Lookup a patient using patiend id
     * @param pid patient id
     * @return The patient if found, null otherwise
     */
    suspend fun getPatient(pid: Long): Patient? {
        return healersDao.getPatient(pid)
    }

    /**
     * Get all activities done after startDate
     * @param startDate The date after which all activities are needed
     * @return flow of a list of all activities starting [startDate]
     */
    fun getActivitiesStarting(startDate: LocalDate): Flow<List<Activity>> {
        return healersDao.getActivities(startDate.atStartOfDay())
    }
}
