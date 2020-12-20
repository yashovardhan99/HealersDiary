package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import com.yashovardhan99.healersdiary.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CreateNewActivity : AppCompatActivity() {
    private val viewModel: CreateActivityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new)
        val pid = intent.getLongExtra(PATIENT_ID, -1)
        if (pid != -1L) {
            viewModel.selectPatient(pid)
            Timber.d("Patient id = $pid")
        }
        viewModel.result.asLiveData().observe(this) { patientId ->
            if (patientId != -1L) {
                Timber.d("Done and dusted")
                finish()
            }
        }
    }

    companion object {
        const val PATIENT_ID = "patient_id"
    }
}