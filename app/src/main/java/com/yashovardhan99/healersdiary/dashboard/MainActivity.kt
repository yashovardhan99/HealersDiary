package com.yashovardhan99.healersdiary.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.create.CreateNewActivity
import com.yashovardhan99.healersdiary.databinding.ActivityMainBinding
import com.yashovardhan99.healersdiary.utils.Request
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: DashboardViewModel by viewModels()
    private val getPid = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent = result.data
        val uri = intent?.data
        if (result.resultCode != RESULT_OK) {
            Timber.d("Result Not Ok = $result")
            return@registerForActivityResult
        }
        Timber.d("Received! Uri = $uri Intent = $intent")
        val authority = uri?.authority
        val path = uri?.path
        val scheme = uri?.scheme
        val queries = uri?.query
        val host = uri?.host
        val port = uri?.port
        Timber.d("authority = $authority scheme = $scheme path = $path queries = $queries host = $host port = $port")
        uri?.let { Timber.d("Request = ${Request.fromUri(it)}") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            Timber.d("$controller Dest = $destination args = $arguments")
        }
        binding.newRecord.setOnClickListener {
            Timber.d("New record")
            val intent = Intent(this, CreateNewActivity::class.java)
            getPid.launch(intent)
        }
    }
}