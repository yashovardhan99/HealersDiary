package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
    }

    companion object {
        const val PATIENT_ID = "patient_id"
    }
}