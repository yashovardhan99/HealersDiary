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
        viewModel.selectType(ExportWorker.Companion.DataType.Patients, it)
    }
    private val exportHealingsLauncher = registerForActivityResult(createDocumentContract) {
        Timber.d("Received uri for export healings = $it")
        viewModel.selectType(ExportWorker.Companion.DataType.Healings, it)
    }
    private val exportPaymentsLauncher = registerForActivityResult(createDocumentContract) {
        Timber.d("Received uri for export payments = $it")
        viewModel.selectType(ExportWorker.Companion.DataType.Payments, it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBackupBinding.inflate(inflater, container, false)
        binding.header = Header.buildHeader(Icons.Back, getString(R.string.backup_sync_group_name))
        binding.start.setOnClickListener {
            if (binding.importExportToggle.checkedButtonId == R.id.export) {
                viewModel.setExport(true)
                if (viewModel.checkedTypes and ExportWorker.Companion.DataType.Patients.mask > 0)
                    exportPatientsLauncher.launch("patients.csv")
                if (viewModel.checkedTypes and ExportWorker.Companion.DataType.Healings.mask > 0)
                    exportHealingsLauncher.launch("healings.csv")
                if (viewModel.checkedTypes and ExportWorker.Companion.DataType.Payments.mask > 0)
                    exportPaymentsLauncher.launch("payments.csv")
            } else viewModel.setExport(false)
        }
        binding.start.isEnabled = viewModel.checkedTypes != 0
        binding.patientCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(ExportWorker.Companion.DataType.Patients)
            else viewModel.deselectType(ExportWorker.Companion.DataType.Patients)
            binding.start.isEnabled = viewModel.checkedTypes != 0
        }
        binding.healingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(ExportWorker.Companion.DataType.Healings)
            else viewModel.deselectType(ExportWorker.Companion.DataType.Healings)
            binding.start.isEnabled = viewModel.checkedTypes != 0
        }
        binding.paymentCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(ExportWorker.Companion.DataType.Payments)
            else viewModel.deselectType(ExportWorker.Companion.DataType.Payments)
            binding.start.isEnabled = viewModel.checkedTypes != 0
        }
        return binding.root
    }
}

object CreateCsvDocument : ActivityResultContracts.CreateDocument() {
    override fun createIntent(context: Context, input: String): Intent {
        return super.createIntent(context, input)
            .setType("text/csv")
    }
}
