package com.yashovardhan99.healersdiary.dashboard

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
import kotlinx.coroutines.flow.map

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

    /**
     * Get all activities
     * @return pager of a list of all activities
     */
    fun getAllActivities(): Flow<PagingData<Activity>> {
        return Pager(
            config = PagingConfig(pageSize = 40, enablePlaceholders = false, initialLoadSize = 80)
        ) {
            healersDao.getAllActivities()
        }.flow
    }

    /**
     * Get total count of all healings done between the specified dates
     * @param startDate The date after which we want to count healings (inclusive)
     * @param endDateInclusive The date before which we want to count healings (inclusive)
     * @return The total count of all healings between the specified dates
     */
    fun getHealingCountBetween(startDate: LocalDate, endDateInclusive: LocalDate): Flow<Int> {
        return healersDao.getHealingCountBetween(
            startDate.atStartOfDay(),
            endDateInclusive.plusDays(1).atStartOfDay().minusNanos(1)
        ).distinctUntilChanged()
    }

    /**
     * Get total charges of all healings done between the specified dates
     * @param startDate The date after which we want to add healings (inclusive)
     * @param endDateInclusive The date before which we want to add healings (inclusive)
     * @return The total sum of charges of all healings between the specified dates
     */
    fun getHealingAmountBetween(startDate: LocalDate, endDateInclusive: LocalDate): Flow<Long> {
        return healersDao.getHealingAmountBetween(
            startDate.atStartOfDay(),
            endDateInclusive.plusDays(1).atStartOfDay().minusNanos(1)
        ).distinctUntilChanged().map { it ?: 0 }
    }
}
