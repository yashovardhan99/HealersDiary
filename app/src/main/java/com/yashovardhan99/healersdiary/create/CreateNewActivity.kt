package com.yashovardhan99.healersdiary.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.core.database.ActivityType
import com.yashovardhan99.core.utils.Request
import com.yashovardhan99.core.utils.Request.Companion.fromUri
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CreateNewActivity : AppCompatActivity() {
    private val viewModel: CreateActivityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new)
        val request = intent.data?.let { uri ->
            Timber.d("Request = $uri")
            fromUri(uri)
        }
        handleRequest(request)
        viewModel.result.asLiveData().observe(this) { requestResult ->
            if (requestResult != null) {
                Intent(Intent.ACTION_VIEW, requestResult.getUri()).also { result ->
                    setResult(Activity.RESULT_OK, result)
                    Timber.d("Setting result = $result")
                }
                Timber.d("Done and dusted")
                finish()
            }
        }
    }

    private fun handleRequest(request: Request?) {
        when (request) {
            is Request.NewHealing -> viewModel.selectPatient(request.patientId, ActivityType.HEALING)
            is Request.NewPayment -> viewModel.selectPatient(request.patientId, ActivityType.PAYMENT)
            Request.NewPatient -> viewModel.newPatient()
            is Request.NewActivity -> viewModel.selectPatient(request.patientId)
            is Request.UpdateHealing -> throw NotImplementedError()
            is Request.UpdatePayment -> throw NotImplementedError()
            Request.ViewDashboard -> finish()
            else -> throw IllegalArgumentException()
        }
    }
}