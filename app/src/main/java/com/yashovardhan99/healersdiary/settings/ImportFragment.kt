package com.yashovardhan99.healersdiary.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yashovardhan99.core.backup_restore.BackupUtils
import com.yashovardhan99.core.backup_restore.BackupUtils.contains
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.buildHeader
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentImportBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class ImportFragment : Fragment() {
    private val viewModel: BackupViewModel by activityViewModels()
    private lateinit var binding: FragmentImportBinding
    private val args by navArgs<ImportFragmentArgs>()
    private val importDocumentContract = object : ActivityResultContracts.OpenDocument() {
        override fun createIntent(context: Context, input: Array<out String>): Intent {
            return super.createIntent(context, input).apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, viewModel.exportUriCopy)
                }
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        }
    }

    private val patientsImportLauncher =
        registerForActivityResult(importDocumentContract) {
            Timber.d("Received uri for import patients = $it")
            if (it != null && viewModel.checkUri(it)) {
                viewModel.selectType(BackupUtils.DataType.Patients, it)
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
    private val healingsImportLauncher =
        registerForActivityResult(importDocumentContract) {
            Timber.d("Received uri for import healings = $it")
            if (it != null && viewModel.checkUri(it)) {
                viewModel.selectType(BackupUtils.DataType.Healings, it)
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
    private val paymentsImportLauncher =
        registerForActivityResult(importDocumentContract) {
            Timber.d("Received uri for import payments = $it")
            if (it != null && viewModel.checkUri(it)) {
                viewModel.selectType(BackupUtils.DataType.Payments, it)
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
            binding.header = buildHeader(Icons.Back, R.string.backup_sync_group_name)
            binding.heading.icon.setOnClickListener {
                findNavController().popBackStack()
            }
        }
        viewModel.setExport(false)
        val selected = args.selectedType
        binding.patientsUploadBox.visibility =
            if (BackupUtils.DataType.Patients in selected) View.VISIBLE
            else View.GONE
        binding.healingsUploadBox.visibility =
            if (BackupUtils.DataType.Healings in selected) View.VISIBLE
            else View.GONE
        binding.paymentsUploadBox.visibility =
            if (BackupUtils.DataType.Payments in selected) View.VISIBLE
            else View.GONE
        binding.patientsUploadBox.setOnClickListener {
            patientsImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values"))
        }
        binding.healingsUploadBox.setOnClickListener {
            healingsImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values"))
        }
        binding.paymentsUploadBox.setOnClickListener {
            paymentsImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values"))
        }
        binding.importBackup.setOnClickListener {
            if (viewModel.isReadyForImport()) {
                viewModel.importBackup()
            }
        }
        lifecycleScope.launchWhenResumed {
            viewModel.showProgress.collect {
                if (it != null) findNavController().navigate(
                    ImportFragmentDirections
                        .actionImportFragmentToBackupProgressFragment(it.toString())
                )
            }
        }

        return binding.root
    }
}
