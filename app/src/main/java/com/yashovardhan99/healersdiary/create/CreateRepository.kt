package com.yashovardhan99.healersdiary.create

import com.yashovardhan99.healersdiary.database.HealersDao
import com.yashovardhan99.healersdiary.database.Healing
import com.yashovardhan99.healersdiary.database.Payment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRepository @Inject constructor(private val healersDao: HealersDao) {
    suspend fun insertNewHealing(healing: Healing) = healersDao.newHealing(healing)
    suspend fun insertNewPayment(payment: Payment) = healersDao.newPayment(payment)
}