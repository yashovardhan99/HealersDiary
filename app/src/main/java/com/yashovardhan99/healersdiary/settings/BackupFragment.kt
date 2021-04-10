package com.yashovardhan99.healersdiary.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.yashovardhan99.core.backup_restore.ExportWorker
import com.yashovardhan99.core.getColorFromAttr
import com.yashovardhan99.core.utils.Header
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentBackupBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class BackupFragment : Fragment() {
    private val viewModel: BackupViewModel by activityViewModels()
    private val createDocumentContract = CreateCsvDocument
    private val exportPatientsLauncher = registerForActivityResult(createDocumentContract) {
        Timber.d("Received uri for export patients = $it")
        if (it != null)
            viewModel.selectType(ExportWorker.Companion.DataType.Patients, it)
    }
    private val exportHealingsLauncher = registerForActivityResult(createDocumentContract) {
        Timber.d("Received uri for export healings = $it")
        if (it != null)
            viewModel.selectType(ExportWorker.Companion.DataType.Healings, it)
    }
    private val exportPaymentsLauncher = registerForActivityResult(createDocumentContract) {
        Timber.d("Received uri for export payments = $it")
        if (it != null)
            viewModel.selectType(ExportWorker.Companion.DataType.Payments, it)
    }
    private lateinit var binding: FragmentBackupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBackupBinding.inflate(inflater, container, false)
        binding.header = Header.buildHeader(Icons.Back, getString(R.string.backup_sync_group_name))
        binding.importExportToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.export -> if (isChecked) {
                    viewModel.setExport(true)
                    hideImportNote()
                }
                R.id.import_backup -> if (isChecked) {
                    viewModel.setExport(false)
                    if (viewModel.checkedTypes > 0)
                        showImportNote(
                            viewModel.checkedTypes and
                                ExportWorker.Companion.DataType.Patients.mask > 0
                        )
                }
            }
        }
        binding.start.setOnClickListener {
            if (binding.importExportToggle.checkedButtonId == R.id.export) {
                viewModel.setExport(true)
                if (viewModel.checkedTypes and ExportWorker.Companion.DataType.Patients.mask > 0)
                    exportPatientsLauncher.launch("patients.csv")
                if (viewModel.checkedTypes and ExportWorker.Companion.DataType.Healings.mask > 0)
                    exportHealingsLauncher.launch("healings.csv")
                if (viewModel.checkedTypes and ExportWorker.Companion.DataType.Payments.mask > 0)
                    exportPaymentsLauncher.launch("payments.csv")
            } else {
                viewModel.setExport(false)
                goToImport()
            }
        }
        binding.start.isEnabled = viewModel.checkedTypes != 0
        binding.patientCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(ExportWorker.Companion.DataType.Patients)
            else viewModel.deselectType(ExportWorker.Companion.DataType.Patients)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(isChecked)
            } else hideImportNote()
        }
        binding.healingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(ExportWorker.Companion.DataType.Healings)
            else viewModel.deselectType(ExportWorker.Companion.DataType.Healings)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(
                    viewModel.checkedTypes and ExportWorker.Companion.DataType.Patients.mask > 0
                )
            } else hideImportNote()
        }
        binding.paymentCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(ExportWorker.Companion.DataType.Payments)
            else viewModel.deselectType(ExportWorker.Companion.DataType.Payments)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(
                    viewModel.checkedTypes and ExportWorker.Companion.DataType.Patients.mask > 0
                )
            } else hideImportNote()
        }
        return binding.root
    }

    private fun showImportNote(patientSelected: Boolean) {
        binding.importNote.setText(
            if (patientSelected) R.string.might_be_overwrriten_note
            else R.string.must_match_an_existing_patient
        )
        context?.let {
            binding.importNote.setTextColor(
                it.getColorFromAttr(
                    if (patientSelected) R.attr.colorOnBackground
                    else R.attr.colorError
                )
            )
        }
        binding.importNote.visibility = View.VISIBLE
    }

    private fun hideImportNote() {
        binding.importNote.visibility = View.GONE
    }

    private fun goToImport() {
        findNavController().navigate(
            BackupFragmentDirections.actionBackupFragmentToImportFragment(
                viewModel.checkedTypes
            )
        )
    }
}

object CreateCsvDocument : ActivityResultContracts.CreateDocument() {
    override fun createIntent(context: Context, input: String): Intent {
        return super.createIntent(context, input)
            .setType("text/csv")
    }
}
