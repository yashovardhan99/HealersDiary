package com.yashovardhan99.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.yashovardhan99.core.DangerousDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

class PatientsDatabaseTest {
    private lateinit var healersDao: HealersDao
    private lateinit var db: HealersDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HealersDatabase::class.java).build()
        healersDao = db.healersDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadPatient() {
        val patient = DbUtils.patients.first()
        runBlocking {
            val id = healersDao.insertPatient(patient)
            assertThat(id).isGreaterThan(0)
            val getPatient = healersDao.getPatient(id)
            assertThat(getPatient).isEqualTo(patient.copy(id = id))
        }
    }

    @Test
    fun writeAndReadPatientList() {
        val patients = DbUtils.patients
        val ids = LongArray(patients.size)
        runBlocking {
            patients.forEachIndexed { i, patient ->
                ids[i] = healersDao.insertPatient(patient)
            }
            val patientList = healersDao.getAllPatients().first()
            assertThat(patientList).hasSize(patients.size)
            assertThat(patientList).isInOrder { p1, p2 ->
                (p1 as Patient).id.compareTo((p2 as Patient).id)
            }
            assertThat(patientList).containsNoDuplicates()
            assertThat(patientList).containsExactlyElementsIn(patients.mapIndexed { index, patient ->
                patient.copy(id = ids[index])
            })
        }
    }

    @Test
    fun patient_updateTest() {
        val patient = DbUtils.patients.last()
        runBlocking {
            val id = healersDao.insertPatient(patient)
            val dbPatient = healersDao.getPatient(id)
            assertThat(dbPatient).isEqualTo(patient.copy(id = id))
            val updatedPatient = dbPatient!!.copy(charge = 200_00, name = "George",
                    notes = "Sample note")
            healersDao.updatePatient(updatedPatient)
            val dbUpdatedPatient = healersDao.getPatient(id)
            assertThat(dbUpdatedPatient).isEqualTo(updatedPatient)
        }
    }

    @Test
    fun deletePatient() {
        val patient = DbUtils.patients.first()
        runBlocking {
            val id = healersDao.insertPatient(patient)
            val dbPatient = healersDao.getPatient(id)
            assertThat(dbPatient).isNotNull()
            healersDao.deletePatientData(dbPatient!!)
            val deletedPatient = healersDao.getPatient(id)
            assertThat(deletedPatient).isNull()
        }
    }

    @Test
    fun deleteAllPatients() {
        val patients = DbUtils.patients
        runBlocking {
            patients.forEach { healersDao.insertPatient(it) }
            val dbPatients = healersDao.getAllPatients().first()
            assertThat(dbPatients).hasSize(patients.size)
            @OptIn(DangerousDatabase::class)
            healersDao.deleteAllPatients()
            val deletedPatients = healersDao.getAllPatients().first()
            assertThat(deletedPatients).isEmpty()
        }
    }
}