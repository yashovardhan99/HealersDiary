package com.yashovardhan99.healersdiary.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface HealersDao {

    @Query("SELECT * FROM patients")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM healings WHERE time>=:startDate")
    fun getAllHealings(startDate: Date): Flow<List<Healing>>

    @Query("SELECT * FROM payments WHERE time>=:startDate")
    fun getAllPayments(startDate: Date): Flow<List<Payment>>

    @Query("SELECT * FROM healings WHERE patient_id == :patientId ORDER BY time DESC")
    fun getAllHealings(patientId: Long): PagingSource<Int, Healing>

    @Query("SELECT * FROM payments WHERE patient_id == :patientId ORDER BY time DESC")
    fun getAllPayments(patientId: Long): PagingSource<Int, Payment>
}