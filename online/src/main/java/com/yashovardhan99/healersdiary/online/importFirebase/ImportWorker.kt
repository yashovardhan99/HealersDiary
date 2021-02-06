package com.yashovardhan99.healersdiary.online.importFirebase

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.database.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.math.BigDecimal
import java.util.*
import kotlin.math.roundToInt

class ImportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        buildNotificationChannels()
        setProgress(0)
        val uid = Firebase.auth.currentUser?.uid ?: return Result.failure()
        Firebase.firestore.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = false
        }
        return try {
            val result = Firebase.firestore.collection(Firestore.USER_KEY)
                    .document(uid)
                    .collection(Firestore.PATIENTS_KEY)
                    .get().await().documents
            Timber.d("$result")
            if (result.isEmpty()) {
                return Result.failure()
            }
            setProgress(0, result.size)
            processPatients(result)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun handleException(e: Exception): Result {
        Timber.e(e)
        return when (e) {
            is FirebaseFirestoreException -> {
                when (e.code) {
                    FirebaseFirestoreException.Code.CANCELLED -> Result.retry()
                    FirebaseFirestoreException.Code.UNKNOWN -> Result.retry()
                    FirebaseFirestoreException.Code.INTERNAL -> Result.retry()
                    FirebaseFirestoreException.Code.UNAVAILABLE -> Result.retry()
                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> Result.failure()
                    else -> Result.failure()
                }
            }
            is FirebaseNetworkException -> {
                Result.retry()
            }
            else -> {
                Result.failure()
            }
        }
    }

    private suspend fun processPatients(patients: List<DocumentSnapshot>): Result {
        val database = DatabaseModule.provideHealersDatabase(applicationContext)
        val dao = DatabaseModule.provideHealersDao(database)
        dao.deleteAllHealings()
        dao.deleteAllPayments()
        dao.deleteAllPatients()
        patients.forEachIndexed { index, patient ->
            val dbPatient = Patient(
                    0,
                    patient.getString(Firestore.NAME_KEY) ?: "ERROR",
                    patient.getAmount(Firestore.CHARGE_KEY),
                    patient.getAmount(Firestore.DUE_KEY),
                    patient.getString(Firestore.NOTES_KEY) ?: "",
                    Date(),
                    patient.getDate(Firestore.CREATED_KEY) ?: Date())
            val id = dao.insertPatient(dbPatient)
            Timber.d("Inserted $dbPatient for id = $id")
            with(dao) { getPatientData(patient.id, id, dbPatient.charge) }
            setProgress(index + 1, patients.size)
        }
        return Result.success()
    }

    private suspend fun HealersDao.getPatientData(patientUid: String, id: Long, charge: Long) {
        val uid = Firebase.auth.uid ?: throw IllegalStateException("Not authenticated")
        val healings = Firebase.firestore.collection(Firestore.USER_KEY)
                .document(uid)
                .collection(Firestore.PATIENTS_KEY)
                .document(patientUid)
                .collection(Firestore.HEALINGS_KEY)
                .get().await().documents
        for (healing in healings) {
            val dbHealing = Healing(0,
                    healing.getDate(Firestore.CREATED_KEY) ?: Date(),
                    charge, "", id)
            insertHealing(dbHealing)
        }
        val payments = Firebase.firestore.collection(Firestore.USER_KEY)
                .document(uid)
                .collection(Firestore.PATIENTS_KEY)
                .document(patientUid)
                .collection(Firestore.PAYMENTS_KEY)
                .get().await().documents
        for (payment in payments) {
            val dbPayment = Payment(
                    0,
                    payment.getDate(Firestore.CREATED_KEY) ?: Date(),
                    payment.getAmount(Firestore.AMOUNT_KEY),
                    "", id)
            insertPayment(dbPayment)
        }
    }

    private suspend fun setProgress(patientsFound: Int) {
        if (patientsFound == 0) {
            setForegroundInfo(0)
            setProgress(workDataOf(
                    OVERALL_PROGRESS to 0,
                    PATIENTS_FOUND to false))
        }
    }

    private suspend fun setProgress(patientsDone: Int, maxPatients: Int) {
        val progress = ((MAX_PROGRESS - INITIAL_PROGRESS) * patientsDone.toFloat() / maxPatients).roundToInt() + INITIAL_PROGRESS
        setForegroundInfo(progress)
        val workData = workDataOf(OVERALL_PROGRESS to progress,
                PATIENTS_DONE to patientsDone,
                MAX_PATIENTS to maxPatients,
                PATIENTS_FOUND to true)
        setProgress(workData)
    }

    private suspend fun setForegroundInfo(progress: Int) {
        val notification = getNotificationBuilder()
                .setProgress(MAX_PROGRESS, progress, false)
                .build()
        val foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1, notification)
        }
        setForeground(foregroundInfo)
    }

    private fun getNotificationBuilder() = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Import from v1.0")

    private fun buildNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelGroup = NotificationChannelGroup(GROUP_ID, applicationContext.getString(GROUP_NAME))
            val notificationChannel = NotificationChannel(CHANNEL_ID, applicationContext.getString(CHANNEL_NAME), NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.group = channelGroup.id
            NotificationManagerCompat.from(applicationContext).createNotificationChannelGroup(channelGroup)
            NotificationManagerCompat.from(applicationContext).createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val MAX_PROGRESS = 100
        const val INITIAL_PROGRESS = 10
        const val GROUP_ID = "online"
        const val GROUP_NAME = com.yashovardhan99.healersdiary.online.R.string.backup_sync_group_name
        const val CHANNEL_ID = "online_import"
        const val CHANNEL_NAME = com.yashovardhan99.healersdiary.online.R.string.import_channel_name
        const val PATIENTS_DONE = "patients_done"
        const val MAX_PATIENTS = "max_patients"
        const val OVERALL_PROGRESS = "progress"
        const val PATIENTS_FOUND = "patients_found"

        object Firestore {
            const val USER_KEY = "users"
            const val PATIENTS_KEY = "patients"
            const val HEALINGS_KEY = "healings"
            const val PAYMENTS_KEY = "payments"
            const val NAME_KEY = "Name"
            const val CHARGE_KEY = "Rate"
            const val DUE_KEY = "Due"
            const val NOTES_KEY = "Disease"
            const val CREATED_KEY = "Date"
            const val AMOUNT_KEY = "Amount"
        }
    }
}

private fun DocumentSnapshot.getAmount(key: String): Long {
    val amount = getDouble(key)?.toBigDecimal()?.multiply(BigDecimal(100))
    return amount?.toLong() ?: 0
}
