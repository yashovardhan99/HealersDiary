package com.yashovardhan99.healersdiary.onboarding

import com.yashovardhan99.healersdiary.database.HealersDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepository @Inject constructor(private val dao: HealersDao) {

    suspend fun deleteAll() {
        dao.deleteAllHealings()
        dao.deleteAllPayments()
        dao.deleteAllPatients()
    }

}
