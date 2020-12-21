package com.yashovardhan99.healersdiary.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.yashovardhan99.healersdiary.create.CreateNewActivity
import com.yashovardhan99.healersdiary.patients.NewPatientActivity

class RequestContract : ActivityResultContract<Request, Request>() {
    override fun createIntent(context: Context, input: Request): Intent {
        val uri = input.getUri()
        return when (input) {
            is Request.NewHealing -> Intent(context, CreateNewActivity::class.java).apply { data = uri }
            is Request.NewPayment -> Intent(context, CreateNewActivity::class.java).apply { data = uri }
            Request.NewPatient -> Intent(context, NewPatientActivity::class.java).apply { data = uri }
            is Request.ViewPatient -> TODO()
            is Request.NewActivity -> Intent(context, CreateNewActivity::class.java).apply { data = uri }
            is Request.UpdateHealing -> TODO()
            is Request.UpdatePayment -> TODO()
            is Request.UpdatePatient -> TODO()
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Request? {
        if (resultCode != Activity.RESULT_OK) return null
        if (intent == null) return null
        return Request.fromUri(intent.data ?: return null)
    }
}