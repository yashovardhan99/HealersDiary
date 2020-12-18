package com.yashovardhan99.healersdiary.create

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.yashovardhan99.healersdiary.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CreateNewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val pid = intent.getIntExtra(PATIENT_ID, -1)
        if (pid != -1) {
//            navController.navigate(// TODO: 19/12/20  )
            Timber.d("Patient id = $pid")
        }
    }

    companion object {
        val PATIENT_ID = "patient_id"
    }
}