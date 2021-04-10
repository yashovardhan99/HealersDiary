package com.yashovardhan99.healersdiary.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yashovardhan99.core.backup_restore.ExportWorker
import com.yashovardhan99.core.utils.Header
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentBackupBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class BackupFragment : Fragment() {
    private val viewModel: BackupViewModel by viewModels()
    private val createDocumentContract = CreateCsvDocument
    private val exportPatientsLauncher = registerForActivityResult(createDocumentContract) {
        Timber.d("Received uri for export patients = $it")
        viewModel.createBackup(ExportWorker.Companion.DataType.Patients, it)
    }
    private val exportHealingsLauncher = registerForActivityResult(createDocumentContract) {
    }
    private val exportPaymentsLauncher = registerForActivityResult(createDocumentContract) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBackupBinding.inflate(inflater, container, false)
        binding.header = Header.buildHeader(Icons.Back, getString(R.string.backup_sync_group_name))
        binding.export.setOnClickListener {
            exportPatientsLauncher.launch("patients.csv")
        }
        viewModel
        return binding.root
    }
}

object CreateCsvDocument : ActivityResultContracts.CreateDocument() {
    override fun createIntent(context: Context, input: String): Intent {
        return super.createIntent(context, input)
            .setType("text/csv")
    }
}
