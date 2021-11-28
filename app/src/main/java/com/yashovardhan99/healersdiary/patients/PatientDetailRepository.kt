package com.yashovardhan99.healersdiary.patients

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yashovardhan99.core.database.Activity
import com.yashovardhan99.core.database.HealersDao
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Payment
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class PatientDetailRepository @Inject constructor(private val healersDao: HealersDao) {
    suspend fun getPatient(patientId: Long) = healersDao.getPatient(patientId)

    fun getAllHealings(patientId: Long): Flow<PagingData<Healing>> {
        return Pager(
            config = PagingConfig(pageSize = 20)
        ) {
            healersDao.getAllHealings(patientId)
        }.flow
    }

    fun getAllPayments(patientId: Long): Flow<PagingData<Payment>> {
        return Pager(
            config = PagingConfig(pageSize = 20)
        ) {
            healersDao.getAllPayments(patientId)
        }.flow
    }

    /**
     * Get all activities for a patientId
     * @param patientId The ID of the patient whose activities are sought
     * @return pager of a list of all activities of the patient
     */
    fun getActivities(patientId: Long): Flow<PagingData<Activity>> {
        return Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false, initialLoadSize = 60)
        ) {
            healersDao.getActivities(patientId)
        }.flow
    }

    /**
     * Get total count of all healings done between the specified dates for a specific patient
     * @param startDate The date after which we want to count healings (inclusive)
     * @param endDateInclusive The date before which we want to count healings (inclusive)
     * @param patientId The patient whose healings are to be queried
     * @return The total count of all healings between the specified dates
     */
    fun getHealingCountBetween(
        startDate: LocalDate,
        endDateInclusive: LocalDate,
        patientId: Long
    ): Flow<Int> {
        return healersDao.getHealingCountBetween(
            startDate.atStartOfDay(),
            endDateInclusive.plusDays(1).atStartOfDay().minusNanos(1),
            patientId
        ).distinctUntilChanged()
    }

    /**
     * Get total charges of all healings done between the specified dates for a specific patient
     * @param startDate The date after which we want to add healings (inclusive)
     * @param endDateInclusive The date before which we want to add healings (inclusive)
     * @param patientId The patient whose healings are to be queried
     * @return The total sum of charges of all healings between the specified dates
     */
    fun getHealingAmountBetween(
        startDate: LocalDate,
        endDateInclusive: LocalDate,
        patientId: Long
    ): Flow<Long> {
        return healersDao.getHealingAmountBetween(
            startDate.atStartOfDay(),
            endDateInclusive.plusDays(1).atStartOfDay().minusNanos(1),
            patientId
        ).distinctUntilChanged().map { it ?: 0 }
    }

    suspend fun deleteHealing(healing: Healing) {
        healersDao.deleteHealing(healing)
    }

    suspend fun deletePayment(payment: Payment) {
        healersDao.deletePayment(payment)
    }
}
