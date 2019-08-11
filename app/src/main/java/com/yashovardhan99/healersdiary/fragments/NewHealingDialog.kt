package com.yashovardhan99.healersdiary.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.activities.MainActivity
import java.text.DateFormat
import java.util.*

/**
 * Created by Yashovardhan99 on 07-08-2018 as a part of HealersDiary.
 */
class NewHealingDialog : DialogFragment(), View.OnClickListener, DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {

    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    internal lateinit var calendar: Calendar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_new_healing_dialog, container, false)
        dateText = rootView.findViewById(R.id.healingDate)
        timeText = rootView.findViewById(R.id.healingTime)
        calendar = Calendar.getInstance()
        dateText.text = DateFormat.getDateInstance().format(calendar.time)
        timeText.text = DateFormat.getTimeInstance().format(calendar.time)
        dateText.setOnClickListener(this)
        timeText.setOnClickListener(this)

        rootView.findViewById<View>(R.id.saveNewHealing).setOnClickListener {
            saveData()
            Snackbar.make(activity!!.findViewById(R.id.patientNameInDetail), R.string.added, Snackbar.LENGTH_SHORT).show()
            dismiss()
        }
        return rootView
    }

    private fun saveData() {
        val uid = activity!!.intent.getStringExtra(MainActivity.PATIENT_UID)
        Log.d("PATIENT UID RECEIVED", uid)
        val db = FirebaseFirestore.getInstance()
        val patient = db.collection("users")
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("patients")
                .document(uid)
        //get patient reference
        patient.get()
                .addOnCompleteListener(OnCompleteListener { task ->
                    var rate = 0.00
                    var due = 0.00
                    if (task.isSuccessful) {
                        Log.d("FIRESTORE", "Data fetched")
                        val data = task.result!!.data
                        if (data == null) {
                            Log.d("FIRESTORE", "Error Data is null")
                            return@OnCompleteListener
                        }

                        if (data.containsKey("Rate") && data["Rate"] != null)
                            rate = java.lang.Double.parseDouble(data["Rate"]!!.toString())
                        if (data.containsKey("Due") && data["Due"] != null)
                            due = java.lang.Double.parseDouble(data["Due"]!!.toString())
                        due += rate
                        patient.update("Due", due)
                                .addOnCompleteListener {
                                    if (!it.isSuccessful) {
                                        Toast.makeText(context, R.string.something_went_wrong_adding_record, Toast.LENGTH_LONG).show()
                                        Log.d("FIRESTORE", it.exception!!.message)
                                    } else
                                        Log.d("FIRESTORE", "Data updated")
                                }
                    }
                })
        val healing = patient.collection("healings")
                .document(Calendar.getInstance().timeInMillis.toString())
        val healingData = HashMap<String, Any>()
        healingData["Date"] = calendar.time
        healing.set(healingData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        Log.d("FIRESTORE", "New Healing data added")
                    else
                        Log.d("FIRESTORE", "Error while adding new healing data : " + task.exception!!.message)
                }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.healingDate -> {
                val datePickerFragment = DatePickerFragment()
                val bundle = Bundle()
                bundle.putLong("DATE", calendar.timeInMillis)
                datePickerFragment.arguments = bundle
                datePickerFragment.setTargetFragment(this, 0)
                datePickerFragment.show(activity!!.supportFragmentManager, "datePicker")
            }
            R.id.healingTime -> {
                val timePickerFragment = TimePickerFragment()
                val time = Bundle()
                time.putLong("DATE", calendar.timeInMillis)
                timePickerFragment.arguments = time
                timePickerFragment.setTargetFragment(this, 0)
                timePickerFragment.show(activity!!.supportFragmentManager, "timePicker")
            }
        }
    }

    override fun onDateSet(dialogFragment: DialogFragment) {
        val dpf = dialogFragment as DatePickerFragment
        calendar.set(dpf.year, dpf.month, dpf.day)
        dateText.text = DateFormat.getDateInstance().format(calendar.time)
        timeText.performClick()
    }

    override fun onTimeSet(dialogFragment: DialogFragment) {
        val tpf = dialogFragment as TimePickerFragment
        calendar.set(Calendar.HOUR_OF_DAY, tpf.hour)
        calendar.set(Calendar.MINUTE, tpf.minute)
        calendar.set(Calendar.SECOND, 0)
        timeText.text = DateFormat.getTimeInstance().format(calendar.time)
    }
}
