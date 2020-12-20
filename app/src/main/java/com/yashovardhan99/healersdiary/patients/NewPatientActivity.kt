package com.yashovardhan99.healersdiary.patients

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
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

@AndroidEntryPoint
class NewPatientActivity : AppCompatActivity() {
    private val viewModel: NewPatientViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityNewPatientBinding>(this, R.layout.activity_new_patient)
        binding.header = Header(getIcon(R.drawable.cross, null, true),
                resources.getString(R.string.add_new_patient),
                getIcon(R.drawable.save, resources.getString(R.string.save), true))
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
        binding.heading.optionsIcon.setOnClickListener { save(binding) }
        viewModel.result.asLiveData().observe(this) { pid ->
            if (pid != -1L) {
                Intent(Intent.ACTION_VIEW, Request.ViewPatient(pid).getUri()).also { result ->
                    setResult(Activity.RESULT_OK, result)
                    Timber.d("Setting result = $result")
                }
                finish()
            }
        }
    }

    private fun save(binding: ActivityNewPatientBinding) {
        val name = binding.nameEdit.text.toString()
        val charge = binding.chargeEdit.text.toString()
        val due = binding.dueEdit.text.toString()
        val notes = binding.notesEdit.text.toString()
        viewModel.createPatient(name, charge, due, notes)
    }
}