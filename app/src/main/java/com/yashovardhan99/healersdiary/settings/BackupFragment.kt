package com.yashovardhan99.healersdiary.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yashovardhan99.core.backup_restore.ExportWorker
import com.yashovardhan99.core.getColorFromAttr
import com.yashovardhan99.core.utils.Header
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentBackupBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

@AndroidEntryPoint
class BackupFragment : Fragment() {
    private val viewModel: BackupViewModel by activityViewModels()
    private val openTreeContract = ActivityResultContracts.OpenDocumentTree()
    private val exportContract = registerForActivityResult(openTreeContract) { uri ->
        Timber.d("Received uri = $uri")
        if (uri != null) {
            viewModel.setExportLocation(requireContext(), uri)
        }
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
                    showExportNote()
                }
                R.id.import_backup -> if (isChecked) {
                    viewModel.setExport(false)
                    if (viewModel.checkedTypes > 0) {
                        showImportNote(
                            viewModel.checkedTypes and
                                ExportWorker.Companion.DataType.Patients.mask > 0
                        )
                    } else showExportNote()
                }
            }
        }
        lifecycleScope.launchWhenResumed {
            viewModel.exportLocation.collect {
                showExportNote()
            }
        }
        binding.start.setOnClickListener {
            if (binding.importExportToggle.checkedButtonId == R.id.export) {
                viewModel.setExport(true)
                lifecycleScope.launchWhenStarted {
                    exportContract.launch(viewModel.exportUriFlow.firstOrNull())
                }
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
            } else showExportNote()
        }
        binding.healingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(ExportWorker.Companion.DataType.Healings)
            else viewModel.deselectType(ExportWorker.Companion.DataType.Healings)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(
                    viewModel.checkedTypes and ExportWorker.Companion.DataType.Patients.mask > 0
                )
            } else showExportNote()
        }
        binding.paymentCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(ExportWorker.Companion.DataType.Payments)
            else viewModel.deselectType(ExportWorker.Companion.DataType.Payments)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(
                    viewModel.checkedTypes and ExportWorker.Companion.DataType.Patients.mask > 0
                )
            } else showExportNote()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (binding.patientCheckbox.isChecked) viewModel.selectType(
            ExportWorker.Companion.DataType.Patients
        )
        else viewModel.deselectType(ExportWorker.Companion.DataType.Patients)
        if (binding.healingCheckbox.isChecked) viewModel.selectType(
            ExportWorker.Companion.DataType.Healings
        )
        else viewModel.deselectType(ExportWorker.Companion.DataType.Healings)
        if (binding.paymentCheckbox.isChecked) viewModel.selectType(
            ExportWorker.Companion.DataType.Payments
        )
        else viewModel.deselectType(ExportWorker.Companion.DataType.Payments)
        binding.importBackup.isChecked = !viewModel.isExporting
        binding.export.isChecked = viewModel.isExporting
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

    private fun showExportNote() {
        if (!viewModel.isExporting) {
            binding.importNote.visibility = View.GONE
            return
        }
        lifecycleScope.launchWhenStarted {
            val exportLocation = viewModel.exportLocation.first()
            if (exportLocation.isNullOrBlank()) {
                binding.importNote.visibility = View.GONE
            } else {
                binding.importNote.text =
                    getString(R.string.exporting_to, exportLocation)
                binding.importNote.visibility = View.VISIBLE
            }
        }
        context?.getColorFromAttr(R.attr.colorOnBackground)?.let {
            binding.importNote.setTextColor(it)
        }
    }

    private fun goToImport() {
        findNavController().navigate(
            BackupFragmentDirections.actionBackupFragmentToImportFragment(
                viewModel.checkedTypes
            )
        )
    }
}
