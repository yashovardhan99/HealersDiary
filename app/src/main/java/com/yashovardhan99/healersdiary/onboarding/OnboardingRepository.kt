package com.yashovardhan99.healersdiary.onboarding

import com.yashovardhan99.core.database.HealersDao
import com.yashovardhan99.core.DangerousDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepository @Inject constructor(private val dao: HealersDao) {

    @DangerousDatabase
    suspend fun deleteAll() {
        dao.deleteAllHealings()
        dao.deleteAllPayments()
        dao.deleteAllPatients()
    }

}
