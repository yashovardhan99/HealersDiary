package com.yashovardhan99.healersdiary.online.importFirebase

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay

class ImportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        setProgress(workDataOf("progress" to 0))
        delay(1000)
        val user = Firebase.auth.currentUser
        setProgress(workDataOf("progress" to 50))
        delay(1000)
        setProgress(workDataOf("progress" to 100))
        return Result.success()
    }

}