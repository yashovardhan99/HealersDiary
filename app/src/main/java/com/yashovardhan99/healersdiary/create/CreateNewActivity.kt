package com.yashovardhan99.healersdiary.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.utils.Request
import com.yashovardhan99.healersdiary.utils.Request.Companion.fromUri
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CreateNewActivity : AppCompatActivity() {
    private val viewModel: CreateActivityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new)
        val request = intent.data?.let { uri ->
            fromUri(uri)
        }
        val pid = if (request is Request.ViewPatient) request.patientId else -1
        if (pid != -1L) {
            viewModel.selectPatient(pid)
            Timber.d("Patient id = $pid")
        }
        viewModel.result.asLiveData().observe(this) { patientId ->
            if (patientId != -1L) {
                Intent(Intent.ACTION_VIEW, Request.ViewPatient(patientId).getUri()).also { result ->
                    setResult(Activity.RESULT_OK, result)
                    Timber.d("Setting result = $result")
                }
                Timber.d("Done and dusted")
                finish()
            }
        }
    }
}