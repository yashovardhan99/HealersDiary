package com.yashovardhan99.healersdiary.dashboard

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yashovardhan99.core.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Database repository for the database.
 * @see DashboardViewModel
 * @see HealersDao
 */

interface DashboardRepository {
    /**
     * Flow of all the patients
     */
    fun getPatients(): Flow<List<Patient>>

    /**
     * Get all healings done after startDate
     * @param startDate The date after which all healings are needed
     * @return flow of a list of all healings starting startDate
     */
    fun getHealingsStarting(startDate: LocalDate): Flow<List<Healing>>

    /**
     * Get all payments done after startDate
     * @param startDate The date after which all payments are needed
     * @return flow of a list of all payments starting startDate
     */
    fun getPaymentsStarting(startDate: LocalDate): Flow<List<Payment>>

    /**
     * Lookup a patient using patiend id
     * @param pid patient id
     * @return The patient if found, null otherwise
     */
    suspend fun getPatient(pid: Long): Patient?

    /**
     * Get all activities done after startDate
     * @param startDate The date after which all activities are needed
     * @return flow of a list of all activities starting [startDate]
     */
    fun getActivitiesStarting(startDate: LocalDate): Flow<List<Activity>>

    /**
     * Get all activities
     * @return pager of a list of all activities
     */
    fun getAllActivities(): Flow<PagingData<Activity>>

    /**
     * Get total count of all healings done between the specified dates
     * @param startDate The date after which we want to count healings (inclusive)
     * @param endDateInclusive The date before which we want to count healings (inclusive)
     * @return The total count of all healings between the specified dates
     */
    fun getHealingCountBetween(startDate: LocalDate, endDateInclusive: LocalDate): Flow<Int>

    /**
     * Get total charges of all healings done between the specified dates
     * @param startDate The date after which we want to add healings (inclusive)
     * @param endDateInclusive The date before which we want to add healings (inclusive)
     * @return The total sum of charges of all healings between the specified dates
     */
    fun getHealingAmountBetween(startDate: LocalDate, endDateInclusive: LocalDate): Flow<Long>

    suspend fun deleteHealing(healing: Healing)

    suspend fun deletePayment(payment: Payment)

}


class DashboardRepositoryImpl @Inject constructor(private val healersDao: HealersDao) :
    DashboardRepository {

    override fun getPatients() = healersDao.getAllPatients().distinctUntilChanged()

    override fun getHealingsStarting(startDate: LocalDate): Flow<List<Healing>> {
        return healersDao.getAllHealings(startDate.atStartOfDay())
    }

    override fun getPaymentsStarting(startDate: LocalDate): Flow<List<Payment>> {
        return healersDao.getAllPayments(startDate.atStartOfDay())
    }

    override suspend fun getPatient(pid: Long): Patient? {
        return healersDao.getPatient(pid)
    }

    override fun getActivitiesStarting(startDate: LocalDate): Flow<List<Activity>> {
        return healersDao.getActivities(startDate.atStartOfDay())
    }

    override fun getAllActivities(): Flow<PagingData<Activity>> {
        return Pager(
            config = PagingConfig(pageSize = 40, enablePlaceholders = false, initialLoadSize = 80)
        ) {
            healersDao.getAllActivities()
        }.flow
    }

    override fun getHealingCountBetween(
        startDate: LocalDate,
        endDateInclusive: LocalDate
    ): Flow<Int> {
        return healersDao.getHealingCountBetween(
            startDate.atStartOfDay(),
            endDateInclusive.plusDays(1).atStartOfDay().minusNanos(1)
        ).distinctUntilChanged()
    }

    override fun getHealingAmountBetween(
        startDate: LocalDate,
        endDateInclusive: LocalDate
    ): Flow<Long> {
        return healersDao.getHealingAmountBetween(
            startDate.atStartOfDay(),
            endDateInclusive.plusDays(1).atStartOfDay().minusNanos(1)
        ).distinctUntilChanged().map { it ?: 0 }
    }

    override suspend fun deleteHealing(healing: Healing) {
        healersDao.deleteHealing(healing)
    }

    override suspend fun deletePayment(payment: Payment) {
        healersDao.deletePayment(payment)
    }
}
