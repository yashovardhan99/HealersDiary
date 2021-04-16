package com.yashovardhan99.healersdiary.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yashovardhan99.core.backup_restore.BackupUtils
import com.yashovardhan99.core.backup_restore.BackupUtils.contains
import com.yashovardhan99.core.getColorFromAttr
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.buildHeader
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
        context?.apply {
            binding.header = buildHeader(Icons.Back, R.string.backup_sync_group_name)
            binding.heading.icon.setOnClickListener {
                findNavController().popBackStack()
            }
        }
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
                                BackupUtils.DataType.Patients.mask > 0
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
        viewModel.showProgress.asLiveData().observe(viewLifecycleOwner) {
            if (it != null) findNavController().navigate(
                BackupFragmentDirections.actionBackupFragmentToBackupProgressFragment(it.toString())
            )
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
            if (isChecked) viewModel.selectType(BackupUtils.DataType.Patients)
            else viewModel.deselectType(BackupUtils.DataType.Patients)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(isChecked)
            } else showExportNote()
        }
        binding.healingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(BackupUtils.DataType.Healings)
            else viewModel.deselectType(BackupUtils.DataType.Healings)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(
                    BackupUtils.DataType.Patients in viewModel.checkedTypes
                )
            } else showExportNote()
        }
        binding.paymentCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(BackupUtils.DataType.Payments)
            else viewModel.deselectType(BackupUtils.DataType.Payments)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(
                    BackupUtils.DataType.Patients in viewModel.checkedTypes
                )
            } else showExportNote()
        }
        return binding.root
    }

    @SuppressLint("BinaryOperationInTimber")
    override fun onResume() {
        super.onResume()
        if (binding.patientCheckbox.isChecked) viewModel.selectType(
            BackupUtils.DataType.Patients
        )
        else viewModel.deselectType(BackupUtils.DataType.Patients)
        if (binding.healingCheckbox.isChecked) viewModel.selectType(
            BackupUtils.DataType.Healings
        )
        else viewModel.deselectType(BackupUtils.DataType.Healings)
        if (binding.paymentCheckbox.isChecked) viewModel.selectType(
            BackupUtils.DataType.Payments
        )
        else viewModel.deselectType(BackupUtils.DataType.Payments)
        Timber.d(
            "Checked => Patient = ${binding.patientCheckbox.isChecked}" +
                " Healing = ${binding.healingCheckbox.isChecked} " +
                "Payments = ${binding.paymentCheckbox.isChecked}"
        )
        Timber.d("Checked type = ${viewModel.checkedTypes}")
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
                getMask(
                    BackupUtils.DataType.Patients,
                    binding.patientCheckbox.isChecked
                ) or getMask(
                    BackupUtils.DataType.Healings,
                    binding.healingCheckbox.isChecked
                ) or getMask(
                    BackupUtils.DataType.Payments,
                    binding.paymentCheckbox.isChecked
                )
            )
        )
    }

    private fun getMask(type: BackupUtils.DataType, include: Boolean): Int {
        return if (include) type.mask
        else 0
    }
}
