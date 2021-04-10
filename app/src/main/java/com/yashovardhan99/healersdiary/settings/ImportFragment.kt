package com.yashovardhan99.healersdiary.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.yashovardhan99.core.backup_restore.ExportWorker
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.buildHeader
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentImportBinding
import timber.log.Timber

class ImportFragment : Fragment() {
    private val viewModel: BackupViewModel by activityViewModels()
    private lateinit var binding: FragmentImportBinding
    private val args by navArgs<ImportFragmentArgs>()
    private val importContract = ActivityResultContracts.OpenDocument()
    private val patientsImportLauncher = registerForActivityResult(importContract) {
        Timber.d("Received uri for import patients = $it")
        if (it != null && viewModel.checkUri(it)) {
            viewModel.selectType(ExportWorker.Companion.DataType.Patients, it)
            binding.patientsUpload.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.check,
                0,
                0,
                0
            )
            binding.patientsFileName.visibility = View.VISIBLE
            binding.patientsFileName.text = viewModel.getFileName(it)
        }
        binding.importBackup.isEnabled = viewModel.isReadyForImport()
    }
    private val healingsImportLauncher = registerForActivityResult(importContract) {
        Timber.d("Received uri for import healings = $it")
        if (it != null && viewModel.checkUri(it)) {
            viewModel.selectType(ExportWorker.Companion.DataType.Healings, it)
            binding.healingsUpload.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.check,
                0,
                0,
                0
            )
            binding.healingsFileName.visibility = View.VISIBLE
            binding.healingsFileName.text = viewModel.getFileName(it)
        }
        binding.importBackup.isEnabled = viewModel.isReadyForImport()
    }
    private val paymentsImportLauncher = registerForActivityResult(importContract) {
        Timber.d("Received uri for import payments = $it")
        if (it != null && viewModel.checkUri(it)) {
            viewModel.selectType(ExportWorker.Companion.DataType.Payments, it)
            binding.paymentsUpload.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.check,
                0,
                0,
                0
            )
            binding.paymentsFileName.visibility = View.VISIBLE
            binding.paymentsFileName.text = viewModel.getFileName(it)
        }
        binding.importBackup.isEnabled = viewModel.isReadyForImport()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImportBinding.inflate(inflater, container, false)
        context?.apply {
            binding.header = buildHeader(Icons.Back, R.string.import_text)
        }
        viewModel.setExport(false)
        val selected = args.selectedType
        binding.patientsUploadBox.visibility =
            if (selected and ExportWorker.Companion.DataType.Patients.mask > 0) View.VISIBLE
            else View.GONE
        binding.healingsUploadBox.visibility =
            if (selected and ExportWorker.Companion.DataType.Healings.mask > 0) View.VISIBLE
            else View.GONE
        binding.paymentsUploadBox.visibility =
            if (selected and ExportWorker.Companion.DataType.Payments.mask > 0) View.VISIBLE
            else View.GONE
        binding.patientsUploadBox.setOnClickListener {
            patientsImportLauncher.launch(arrayOf("text/csv"))
        }
        binding.healingsUploadBox.setOnClickListener {
            healingsImportLauncher.launch(arrayOf("text/csv"))
        }
        binding.paymentsUploadBox.setOnClickListener {
            paymentsImportLauncher.launch(arrayOf("text/csv"))
        }
        return binding.root
    }
}
