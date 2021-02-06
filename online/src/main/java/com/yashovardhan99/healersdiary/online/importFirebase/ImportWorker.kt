package com.yashovardhan99.healersdiary.online.importFirebase

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
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
import com.yashovardhan99.healersdiary.database.*
import com.yashovardhan99.healersdiary.onboarding.OnboardingViewModel
import com.yashovardhan99.healersdiary.onboarding.SplashActivity
import com.yashovardhan99.healersdiary.online.R
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.math.BigDecimal
import java.util.*
import kotlin.math.roundToInt

class ImportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(com.yashovardhan99.healersdiary.R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.import_v1))
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setColor(ContextCompat.getColor(applicationContext, com.yashovardhan99.healersdiary.R.color.colorPrimaryDark))
            .setOngoing(true)
            .setOnlyAlertOnce(true)

    override suspend fun doWork(): Result {
        updatePreferences(false)
        buildNotificationChannels()
        setProgress(0)
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            showDoneNotification(isSuccessful = false, willRetry = false)
            return Result.failure()
        }
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
                showDoneNotification(isSuccessful = false, willRetry = false)
                return Result.failure()
            }
            setProgress(0, result.size, 0f)
            processPatients(result)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun handleException(e: Exception): Result {
        Timber.e(e)
        val status = when (e) {
            is FirebaseFirestoreException -> {
                when (e.code) {
                    FirebaseFirestoreException.Code.NOT_FOUND -> Result.failure()
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> Result.failure()
                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> Result.failure()
                    else -> Result.retry()
                }
            }
            is FirebaseNetworkException -> {
                Result.retry()
            }
            else -> {
                Result.retry()
            }
        }
        if (status is Result.Retry) showDoneNotification(isSuccessful = false, willRetry = true)
        else showDoneNotification(isSuccessful = false, willRetry = false)
        return status
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
            setProgress(index + 1, patients.size, INITIAL_PROGRESS_FLOAT)
            with(dao) { getPatientData(patient.id, id, dbPatient.charge, index + 1, patients.size) }
            setProgress(index + 1, patients.size, 1f)
        }
        updatePreferences(true)
        showDoneNotification(isSuccessful = true, willRetry = false)
        return Result.success()
    }

    private suspend fun updatePreferences(importCompleted: Boolean) {
        DatabaseModule.provideAppDatastore(applicationContext).edit { preferences ->
            preferences[OnboardingViewModel.Companion.PreferencesKey.importComplete] = importCompleted
        }
    }

    private suspend fun HealersDao.getPatientData(patientUid: String, id: Long, charge: Long, curIndex: Int, maxPatients: Int) {
        val uid = Firebase.auth.uid ?: throw IllegalStateException("Not authenticated")
        val healings = Firebase.firestore.collection(Firestore.USER_KEY)
                .document(uid)
                .collection(Firestore.PATIENTS_KEY)
                .document(patientUid)
                .collection(Firestore.HEALINGS_KEY)
                .get().await().documents
        val payments = Firebase.firestore.collection(Firestore.USER_KEY)
                .document(uid)
                .collection(Firestore.PATIENTS_KEY)
                .document(patientUid)
                .collection(Firestore.PAYMENTS_KEY)
                .get().await().documents
        val total = healings.size + payments.size
        healings.forEachIndexed { index, healing ->
            val dbHealing = Healing(0,
                    healing.getDate(Firestore.CREATED_KEY) ?: Date(),
                    charge, "", id)
            insertHealing(dbHealing)
            setProgress(curIndex, maxPatients, INITIAL_PROGRESS_FLOAT + (1 - INITIAL_PROGRESS_FLOAT) * (index + 1).toFloat() / total)
        }
        payments.forEachIndexed { index, payment ->
            val dbPayment = Payment(
                    0,
                    payment.getDate(Firestore.CREATED_KEY) ?: Date(),
                    payment.getAmount(Firestore.AMOUNT_KEY),
                    "", id)
            insertPayment(dbPayment)
            setProgress(curIndex, maxPatients, INITIAL_PROGRESS_FLOAT + (1 - INITIAL_PROGRESS_FLOAT) * (healings.size + (index + 1).toFloat()) / total)
        }
    }

    private suspend fun setProgress(patientsFound: Int) {
        if (patientsFound == 0) {
            setForegroundInfo(0, applicationContext.getString(R.string.looking_for_patients))
            setProgress(workDataOf(
                    OVERALL_PROGRESS to 0,
                    PATIENTS_FOUND to false))
        }
    }

    private suspend fun setProgress(currentPatient: Int, maxPatients: Int, @FloatRange(from = 0.0, to = 1.0) curPercent: Float) {
        val progress = if (currentPatient == 0) INITIAL_PROGRESS
        else {
            val contribution = (MAX_PROGRESS - INITIAL_PROGRESS).toFloat() / maxPatients
            (contribution * (currentPatient - 1) + contribution * curPercent + INITIAL_PROGRESS).roundToInt()
        }
        val message = if (currentPatient == 0) applicationContext.getString(R.string.patients_found, maxPatients)
        else applicationContext.getString(R.string.importing_of, currentPatient, maxPatients)
        setForegroundInfo(progress, message)
        val workData = workDataOf(OVERALL_PROGRESS to progress,
                CURRENT_PATIENT to currentPatient,
                MAX_PATIENTS to maxPatients,
                PATIENTS_FOUND to true)
        setProgress(workData)
    }

    private suspend fun setForegroundInfo(@IntRange(from = 0, to = MAX_PROGRESS.toLong()) progress: Int, message: String) {
        val notification = notificationBuilder
                .setProgress(MAX_PROGRESS, progress, false)
                .setContentText(message)
                .build()
        val foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(PROGRESS_NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(PROGRESS_NOTIF_ID, notification)
        }
        setForeground(foregroundInfo)
    }

    private fun showDoneNotification(isSuccessful: Boolean, willRetry: Boolean) {
        if (!willRetry) Firebase.auth.signOut()
        val intent = Intent(applicationContext, SplashActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(com.yashovardhan99.healersdiary.R.drawable.ic_launcher_foreground)
                .setContentTitle(applicationContext.getString(R.string.import_v1))
                .setContentText(applicationContext.getString(
                        when {
                            isSuccessful -> com.yashovardhan99.healersdiary.R.string.import_completed
                            willRetry -> R.string.import_retry
                            else -> R.string.import_failed
                        })
                )
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setColor(ContextCompat.getColor(applicationContext, com.yashovardhan99.healersdiary.R.color.colorPrimaryDark))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        NotificationManagerCompat.from(applicationContext).notify(COMPLETE_NOTIF_ID, notification)
    }

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
        private const val PROGRESS_NOTIF_ID = 100
        private const val COMPLETE_NOTIF_ID = 200
        const val MAX_PROGRESS = 100
        const val INITIAL_PROGRESS = 10
        const val INITIAL_PROGRESS_FLOAT = 0.1f
        const val GROUP_ID = "online"
        const val GROUP_NAME = R.string.backup_sync_group_name
        const val CHANNEL_ID = "online_import"
        const val CHANNEL_NAME = R.string.import_channel_name
        const val CURRENT_PATIENT = "patients_done"
        const val MAX_PATIENTS = "max_patients"
        const val OVERALL_PROGRESS = "progress"
        const val PATIENTS_FOUND = "patients_found"

        private object Firestore {
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
