package com.yashovardhan99.healersdiary.patients

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.yashovardhan99.core.analytics.AnalyticsEvent
import com.yashovardhan99.core.utils.Icons
import com.yashovardhan99.core.utils.Request
import com.yashovardhan99.core.utils.buildHeader
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.create.CreateNewActivity
import com.yashovardhan99.healersdiary.dashboard.MainActivity
import com.yashovardhan99.healersdiary.databinding.ActivityNewPatientBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

/**
 * Activity to create/edit a patient
 *
 * To launch this activity, use [RequestContract][com.yashovardhan99.core.utils.RequestContract] and pass in either [Request.NewPatient] or [Request.UpdatePatient].
 * Alternatively, if you must create an explicit intent yourself, make sure to pass the URI from the [Request] object
 *
 * If a patient is successfully created/modified, this launches a [Request.ViewPatient] with [Intent.ACTION_VIEW] and the patient id.
 * If a patient is deleted, this launches a [Request.ViewDashboard] with [Intent.ACTION_VIEW].
 * In both cases, [setResult] will be called with [Activity.RESULT_OK].
 * @see NewPatientViewModel
 * @see CreateNewActivity
 */
@AndroidEntryPoint
class NewPatientActivity : AppCompatActivity() {
    private val viewModel: NewPatientViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityNewPatientBinding>(
            this,
            R.layout.activity_new_patient
        )
        // setting the request data in the ViewModel
        intent.data?.let {
            viewModel.setRequest(it)
        }
        // to show the correct currency symbol as prefix
        binding.chargeBox.prefixText = NumberFormat.getCurrencyInstance().currency?.symbol
        binding.dueBox.prefixText = NumberFormat.getCurrencyInstance().currency?.symbol

        // for displaying error and disabling the save button if name is blank
        binding.nameEdit.doAfterTextChanged { text ->
            if (text.isNullOrBlank()) {
                binding.nameEdit.error = resources.getString(R.string.name_cannot_be_blank)
                binding.newPatient.isEnabled = false
            } else {
                binding.nameEdit.error = null
                binding.newPatient.isEnabled = true
            }
        }
        /**
         * This is used to format the amount entered by the user in both the currency editTexts.
         *
         * If the amount is not a number, the EditText is cleared off
         */
        val amountFocusListener = View.OnFocusChangeListener { v, hasFocus ->
            val inputText = (v as TextInputEditText).text.toString()
            try {
                if (!hasFocus && inputText.isNotBlank()) {
                    val edited =
                        BigDecimal(inputText).setScale(2, RoundingMode.HALF_EVEN).toPlainString()
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
        // If the patient is being edited, the top right icon is for delete, otherwise, it is for saving
        binding.heading.optionsIcon.setOnClickListener {
            if (viewModel.patient.value != null) delete()
            else save(binding)
        }
        // Here, the result in ViewModel is observed to either indicate successful creation/editing or deletion
        viewModel.result.asLiveData().observe(this) { result ->
            val patient = viewModel.patient.value
            Timber.d("Patient = $patient")
            when (result) {
                is NewPatientViewModel.Companion.Result.Success -> {
                    // On create/edit -> go to the patient detail fragment
                    Intent(
                        Intent.ACTION_VIEW,
                        Request.ViewPatient(result.patientId).getUri()
                    ).also { res ->
                        setResult(Activity.RESULT_OK, res)
                        Timber.d("Setting result = $res")
                        if (patient != null) {
                            // Push dynamic shortcut for new/updated patient
                            val intent = Intent(Intent.ACTION_VIEW)
                                .setClass(this, MainActivity::class.java)
                                .setData(Request.ViewPatient(result.patientId).getUri())
                            val person =
                                Person.Builder().setName(patient.name).setBot(false).build()
                            val shortcutInfo =
                                ShortcutInfoCompat.Builder(this, "patient_${result.patientId}")
                                    .setShortLabel(patient.name)
                                    .setPerson(person)
                                    .setIntent(intent)
                                    .addCapabilityBinding(
                                        "actions.intent.GET_THING",
                                        "thing.name",
                                        listOf(patient.name)
                                    ).build()
                            ShortcutManagerCompat.pushDynamicShortcut(this, shortcutInfo)
                        }
                        finish()
                    }
                }
                NewPatientViewModel.Companion.Result.Deleted -> {
                    // On deletion -> go to the dashboard
                    Intent(Intent.ACTION_VIEW, Request.ViewDashboard.getUri()).also {
                        setResult(Activity.RESULT_OK, it)
                        Timber.d("Patient deleted!")
                        finish()
                    }
                    if (patient?.id != null) {
                        ShortcutManagerCompat.disableShortcuts(
                            this,
                            listOf("patient_${patient.id}"),
                            getString(R.string.patient_deleted)
                        )
                        ShortcutManagerCompat.removeDynamicShortcuts(
                            this,
                            listOf("patient_${patient.id}")
                        )
                    }
                }
                NewPatientViewModel.Companion.Result.Unset -> {
                }
            }
        }
        // For displaying errors
        viewModel.error.asLiveData().observe(this) { error ->
            if (error) {
                Snackbar.make(
                    binding.newPatient,
                    R.string.error_creating_activity,
                    Snackbar.LENGTH_LONG
                ).show()
                viewModel.resetError() // To avoid error re-propagating. TODO: Change this flow to a channel to avoid this
            }
        }
        // Used for edit operations -> get the patient data and update UI
        // TODO: 25/2/21 Will change this to a normal suspend call
        viewModel.patient.asLiveData().observe(this) { patient ->
            if (patient != null) {
                binding.nameEdit.setText(patient.name)
                binding.newPatient.setText(R.string.update)
                binding.chargeEdit.setText(
                    patient.charge.toBigDecimal().movePointLeft(2).toPlainString()
                )
                binding.dueEdit.setText(patient.due.toBigDecimal().movePointLeft(2).toPlainString())
                binding.notesEdit.setText(patient.notes)
                binding.header = buildHeader(Icons.Close, R.string.edit_patient, Icons.Delete)
            } else {
                binding.newPatient.setText(R.string.add_new_patient)
                binding.header = buildHeader(Icons.Close, R.string.add_new_patient, Icons.Save)
            }

        }
        // headingIcon -> close button
        binding.heading.icon.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvent.Screen.CreatePatient.trackView()
    }

    /**
     * Used to delete the current patient being edited.
     *
     * Shows an alert dialog asking for confirmation.
     * On confirmation, all data relating to that patient is deleted.
     */
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

    /**
     * Save the patient data (either modify or create a new patient)
     *
     * Any error in the data is informed by the ViewModel
     */
    private fun save(binding: ActivityNewPatientBinding) {
        val name = binding.nameEdit.text.toString()
        val charge = binding.chargeEdit.text.toString()
        val due = binding.dueEdit.text.toString()
        val notes = binding.notesEdit.text.toString()
        viewModel.save(name, charge, due, notes)
    }
}