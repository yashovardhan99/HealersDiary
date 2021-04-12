package com.yashovardhan99.healersdiary.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.WorkInfo
import com.yashovardhan99.core.backup_restore.ExportWorker
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
                if (!findNavController().popBackStack(
                        R.id.backupFragment,
                        false
                    )
                ) {
                    findNavController().navigate(
                        R.id.home,
                        null,
                        NavOptions.Builder().setPopUpTo(R.id.home, true)
                            .build()
                    )
                }
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
        binding.subtitle.text = progress.getString(ExportWorker.PROGRESS_TEXT_KEY)
        binding.mainProgress.progress = progress.getInt(ExportWorker.PROGRESS_PERCENT_KEY, 0)
        binding.mainProgress.show()
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
