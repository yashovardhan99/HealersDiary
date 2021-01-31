package com.yashovardhan99.healersdiary.patients

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.ActivityNewPatientBinding
import com.yashovardhan99.healersdiary.utils.Header
import com.yashovardhan99.healersdiary.utils.Request
import com.yashovardhan99.healersdiary.utils.getIcon
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

@AndroidEntryPoint
class NewPatientActivity : AppCompatActivity() {
    private val viewModel: NewPatientViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityNewPatientBinding>(this, R.layout.activity_new_patient)
        intent.data?.let {
            viewModel.setRequest(it)
        }
        binding.chargeBox.prefixText = NumberFormat.getCurrencyInstance().currency?.symbol
        binding.dueBox.prefixText = NumberFormat.getCurrencyInstance().currency?.symbol
        binding.nameEdit.doAfterTextChanged { text ->
            if (text.isNullOrBlank()) {
                binding.nameEdit.error = resources.getString(R.string.name_cannot_be_blank)
                binding.newPatient.isEnabled = false
            } else {
                binding.nameEdit.error = null
                binding.newPatient.isEnabled = true
            }
        }
        val amountFocusListener = View.OnFocusChangeListener { v, hasFocus ->
            val inputText = (v as TextInputEditText).text.toString()
            try {
                if (!hasFocus && inputText.isNotBlank()) {
                    val edited = BigDecimal(inputText).setScale(2, RoundingMode.HALF_EVEN).toPlainString()
                    v.setText(edited)
                } else if (inputText.isNotBlank() && BigDecimal(inputText) == BigDecimal.ZERO) {
                    v.selectAll()
                }
            } catch (e: NumberFormatException) {
                Timber.w(e, "Invalid input")
                v.text?.clear()
            }
        }
        binding.chargeEdit.onFocusChangeListener = amountFocusListener
        binding.dueEdit.onFocusChangeListener = amountFocusListener
        binding.newPatient.setOnClickListener { save(binding) }
        binding.heading.optionsIcon.setOnClickListener {
            if (viewModel.patient.value != null) delete()
            else save(binding)
        }
        viewModel.result.asLiveData().observe(this) { result ->
            when (result) {
                is NewPatientViewModel.Companion.Result.Success -> {
                    Intent(Intent.ACTION_VIEW, Request.ViewPatient(result.patientId).getUri()).also { res ->
                        setResult(Activity.RESULT_OK, res)
                        Timber.d("Setting result = $res")
                        finish()
                    }
                }
                NewPatientViewModel.Companion.Result.Deleted -> {
                    Intent(Intent.ACTION_VIEW, Request.ViewDashboard.getUri()).also {
                        setResult(Activity.RESULT_OK, it)
                        Timber.d("Patient deleted!")
                        finish()
                    }
                }
                NewPatientViewModel.Companion.Result.Unset -> {
                }
            }
        }
        viewModel.error.asLiveData().observe(this) { error ->
            if (error) {
                Snackbar.make(binding.newPatient, R.string.error_creating_activity, Snackbar.LENGTH_LONG).show()
                viewModel.resetError()
            }
        }
        viewModel.patient.asLiveData().observe(this) { patient ->
            if (patient != null) {
                binding.nameEdit.setText(patient.name)
                binding.newPatient.setText(R.string.update)
                binding.chargeEdit.setText(patient.charge.toBigDecimal().movePointLeft(2).toPlainString())
                binding.dueEdit.setText(patient.due.toBigDecimal().movePointLeft(2).toPlainString())
                binding.notesEdit.setText(patient.notes)
                binding.header = Header(getIcon(R.drawable.cross, null, true),
                        resources.getString(R.string.edit_patient),
                        getIcon(R.drawable.ic_baseline_delete_forever_24, null, true))
            } else {
                binding.newPatient.setText(R.string.add_new_patient)
                binding.header = Header(getIcon(R.drawable.cross, null, true),
                        resources.getString(R.string.add_new_patient),
                        getIcon(R.drawable.save, resources.getString(R.string.save), true))
            }

        }
        binding.heading.icon.setOnClickListener { finish() }
    }

    private fun delete() {
        AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_warning_message)
                .setIcon(R.drawable.ic_baseline_delete_forever_24)
                .setPositiveButton(R.string.delete) { dialogInterface: DialogInterface, _: Int ->
                    viewModel.deletePatient()
                    dialogInterface.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }.show()
    }

    private fun save(binding: ActivityNewPatientBinding) {
        val name = binding.nameEdit.text.toString()
        val charge = binding.chargeEdit.text.toString()
        val due = binding.dueEdit.text.toString()
        val notes = binding.notesEdit.text.toString()
        viewModel.save(name, charge, due, notes)
    }
}