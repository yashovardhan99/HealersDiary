package com.yashovardhan99.healersdiary.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.WorkInfo
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.yashovardhan99.core.backup_restore.BackupUtils
import com.yashovardhan99.core.backup_restore.ImportWorker
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.buildHeader
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentBackupProgressBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class BackupProgressFragment : Fragment() {
    private val viewModel: BackupViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBackupProgressBinding.inflate(inflater, container, false)
        context?.apply {
            binding.header = buildHeader(Icons.Back, R.string.backup_sync_group_name)
            binding.heading.icon.setOnClickListener {
                viewModel.resetShowProgress()
                findNavController().popBackStack()
            }
        }
        viewModel.workObserver.observe(viewLifecycleOwner) { workInfo ->
            when {
                workInfo == null -> resetDisplay(binding)
                workInfo.state == WorkInfo.State.ENQUEUED -> {
                    Timber.d("Enqueued Work = $workInfo")
                    resetDisplay(binding)
                }
                workInfo.tags.contains("importWorker") -> showImportData(binding, workInfo.progress)
                workInfo.tags.contains("exportWorker") -> showExportData(binding, workInfo.progress)
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.resetShowProgress()
    }

    private fun resetDisplay(binding: FragmentBackupProgressBinding) {
        binding.title.visibility = View.GONE
        binding.subtitle.visibility = View.VISIBLE
        binding.subtitle.setText(R.string.not_much_to_show_here)
        binding.mainProgress.hide()
        binding.patientsBox.visibility = View.GONE
        binding.healingsBox.visibility = View.GONE
        binding.paymentsBox.visibility = View.GONE
    }

    private fun showExportData(binding: FragmentBackupProgressBinding, progress: Data) {
        if (binding.mainProgress.isIndeterminate) {
            binding.mainProgress.visibility = View.INVISIBLE
            binding.mainProgress.isIndeterminate = false
        }
        if (binding.patientProgress.isIndeterminate) {
            binding.patientProgress.visibility = View.INVISIBLE
            binding.patientProgress.isIndeterminate = false
        }
        if (binding.healingProgress.isIndeterminate) {
            binding.healingProgress.visibility = View.INVISIBLE
            binding.healingProgress.isIndeterminate = false
        }
        if (binding.paymentProgress.isIndeterminate) {
            binding.paymentProgress.visibility = View.INVISIBLE
            binding.paymentProgress.isIndeterminate = false
        }
        binding.title.visibility = View.VISIBLE
        binding.title.setText(R.string.exporting_data)
        binding.subtitle.text = progress.getString(BackupUtils.Progress.ProgressMessage)
        val dataType = progress.getInt(BackupUtils.Progress.RequiredBit, 0)
        val current = progress.getInt(BackupUtils.Progress.CurrentBit, 0)
        var curProgress = progress.getInt(BackupUtils.Progress.ProgressPercent, 0)
        var max = 0
        val error = progress.getInt(BackupUtils.Progress.FileErrorBit, 0)
        val done = progress.getIntArray(BackupUtils.Progress.ExportCounts)
        val counts = progress.getIntArray(BackupUtils.Progress.ExportTotal)
        if (dataType and BackupUtils.DataType.Patients.mask > 0) {
            // patient included
            max += 50
            if (current > BackupUtils.DataType.Patients.mask) curProgress += 50
            binding.patientsBox.visibility = View.VISIBLE
            setExportSubView(
                binding.patientStatus, binding.patientProgress,
                error and BackupUtils.DataType.Patients.mask > 0,
                done?.get(0) ?: 0, counts?.get(0) ?: 0,
                current >= BackupUtils.DataType.Patients.mask
            )
        } else binding.patientsBox.visibility = View.GONE
        if (dataType and BackupUtils.DataType.Healings.mask > 0) {
            max += 100
            if (current > BackupUtils.DataType.Healings.mask) curProgress += 100
            binding.healingsBox.visibility = View.VISIBLE
            setExportSubView(
                binding.healingStatus, binding.healingProgress,
                error and BackupUtils.DataType.Healings.mask > 0,
                done?.get(1) ?: 0, counts?.get(1) ?: 0,
                current >= BackupUtils.DataType.Healings.mask
            )
        } else binding.healingsBox.visibility = View.GONE
        if (dataType and BackupUtils.DataType.Payments.mask > 0) {
            max += 50
            if (current > BackupUtils.DataType.Payments.mask) curProgress += 50
            binding.paymentsBox.visibility = View.VISIBLE
            setExportSubView(
                binding.paymentStatus, binding.paymentProgress,
                error and BackupUtils.DataType.Payments.mask > 0,
                done?.get(2) ?: 0, counts?.get(2) ?: 0,
                current >= BackupUtils.DataType.Payments.mask
            )
        } else binding.paymentsBox.visibility = View.GONE
        binding.mainProgress.max = max
        binding.mainProgress.setProgressCompat(curProgress, true)
    }

    private fun setExportSubView(
        statusTextView: TextView,
        progressBar: LinearProgressIndicator,
        isError: Boolean,
        done: Int,
        count: Int,
        ready: Boolean
    ) {
        if (ready) {
            statusTextView.text = getString(R.string.exported_d_of_d, done, count)
            progressBar.max = count
            progressBar.setProgressCompat(done, true)
        } else {
            progressBar.progress = 0
            statusTextView.setText(R.string.waiting)
        }
        if (isError) {
            statusTextView.setText(R.string.file_error)
            progressBar.setProgressCompat(0, true)
        }
        progressBar.show()
    }

    private fun showImportData(binding: FragmentBackupProgressBinding, progress: Data) {
        if (!binding.mainProgress.isIndeterminate) {
            binding.mainProgress.visibility = View.INVISIBLE
            binding.mainProgress.isIndeterminate = true
        }
        if (!binding.patientProgress.isIndeterminate) {
            binding.patientProgress.visibility = View.INVISIBLE
            binding.patientProgress.isIndeterminate = true
        }
        if (!binding.healingProgress.isIndeterminate) {
            binding.healingProgress.visibility = View.INVISIBLE
            binding.healingProgress.isIndeterminate = true
        }
        if (!binding.paymentProgress.isIndeterminate) {
            binding.paymentProgress.visibility = View.INVISIBLE
            binding.paymentProgress.isIndeterminate = true
        }
        binding.title.visibility = View.VISIBLE
        binding.title.setText(R.string.importing_data)
        binding.subtitle.text = progress.getString(ImportWorker.PROGRESS_TEXT_KEY)
        binding.mainProgress.show()
    }
}
