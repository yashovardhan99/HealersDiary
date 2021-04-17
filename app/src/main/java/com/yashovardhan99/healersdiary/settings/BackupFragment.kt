package com.yashovardhan99.healersdiary.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import com.google.android.material.snackbar.Snackbar
import com.yashovardhan99.core.backup_restore.BackupUtils
import com.yashovardhan99.core.backup_restore.BackupUtils.contains
import com.yashovardhan99.core.database.BackupState
import com.yashovardhan99.core.getColorFromAttr
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.buildHeader
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentBackupBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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
                    hideImportNote()
                }
                R.id.import_backup -> if (isChecked) {
                    viewModel.setExport(false)
                    if (viewModel.checkedTypes > 0) {
                        showImportNote(
                            viewModel.checkedTypes and
                                BackupUtils.DataType.Patients.mask > 0
                        )
                    } else hideImportNote()
                }
            }
        }
        viewModel.showProgress.asLiveData().observe(viewLifecycleOwner) {
            if (it != null) findNavController().navigate(
                BackupFragmentDirections.actionBackupFragmentToBackupProgressFragment(it.toString())
            )
        }
        viewModel.getBackupState().asLiveData().observe(viewLifecycleOwner) { backupState ->
            when (backupState) {
                is BackupState.LastRunFailed -> {
                    binding.lastBackupTime.visibility = View.VISIBLE
                    binding.lastBackupNote.visibility = View.VISIBLE
                    context?.getColorFromAttr(R.attr.colorError)?.let {
                        binding.lastBackupNote.setTextColor(it)
                    }
                    setLastBackupTime(backupState.instant)
                    binding.lastBackupNote.setText(R.string.backup_failed)
                    binding.lastBackupTime.setOnClickListener(null)
                    binding.lastBackupNote.setOnClickListener(null)
                }
                is BackupState.LastRunSuccess -> {
                    binding.lastBackupTime.visibility = View.VISIBLE
                    binding.lastBackupNote.visibility = View.VISIBLE
                    binding.lastBackupNote.setTextColor(Color.BLACK)
                    setLastBackupTime(backupState.instant)
                    val (patientsCount, healingsCount, paymentsCount) = backupState.backedUp
                    val patients = resources.getQuantityString(
                        R.plurals.n_patients, patientsCount, patientsCount
                    )
                    val healings = resources.getQuantityString(
                        R.plurals.n_healings, healingsCount, healingsCount
                    )
                    val payments = resources.getQuantityString(
                        R.plurals.n_payments, paymentsCount, paymentsCount
                    )
                    Timber.d("${backupState.backedUp}")
                    Timber.d("$patients $healings $payments")
                    val str = when {
                        patientsCount == 0 -> {
                            when {
                                healingsCount == 0 -> payments
                                paymentsCount == 0 -> healings
                                else -> getString(R.string.join_2, healings, payments)
                            }
                        }
                        healingsCount == 0 -> {
                            when (paymentsCount) {
                                0 -> patients
                                else -> getString(R.string.join_2, patients, payments)
                            }
                        }
                        paymentsCount == 0 -> getString(R.string.join_2, patients, healings)
                        else -> getString(R.string.join_3, patients, healings, payments)
                    }
                    binding.lastBackupNote.text = getString(R.string.last_backup_note, str)
                    binding.lastBackupNote.setOnClickListener {
                        openFiles(backupState.exportFolder)
                    }
                    binding.lastBackupTime.setOnClickListener {
                        openFiles(backupState.exportFolder)
                    }
                }
                else -> {
                    binding.lastBackupTime.visibility = View.GONE
                    binding.lastBackupNote.visibility = View.GONE
                }
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
            if (isChecked) viewModel.selectType(BackupUtils.DataType.Patients)
            else viewModel.deselectType(BackupUtils.DataType.Patients)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(isChecked)
            } else hideImportNote()
        }
        binding.healingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(BackupUtils.DataType.Healings)
            else viewModel.deselectType(BackupUtils.DataType.Healings)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(
                    BackupUtils.DataType.Patients in viewModel.checkedTypes
                )
            } else hideImportNote()
        }
        binding.paymentCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.selectType(BackupUtils.DataType.Payments)
            else viewModel.deselectType(BackupUtils.DataType.Payments)
            binding.start.isEnabled = viewModel.checkedTypes != 0
            if (!viewModel.isExporting && viewModel.checkedTypes > 0) {
                showImportNote(
                    BackupUtils.DataType.Patients in viewModel.checkedTypes
                )
            } else hideImportNote()
        }

        viewModel.errorMessage.asLiveData().observe(viewLifecycleOwner) {
            if (it != null) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.resetError()
            }
        }

        return binding.root
    }

    private fun setLastBackupTime(instant: Instant) {
        binding.lastBackupTime.text = getString(
            R.string.last_backup_time,
            LocalDateTime.ofInstant(
                instant, ZoneId.systemDefault()
            ).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))
        )
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

    private fun hideImportNote() {
        binding.importNote.visibility = View.GONE
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

    private fun openFiles(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Timber.w(e, "Error opening file uri %s", uri.toString())
            Snackbar.make(binding.root, getString(R.string.cant_find_backup), Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun getMask(type: BackupUtils.DataType, include: Boolean): Int {
        return if (include) type.mask
        else 0
    }
}
