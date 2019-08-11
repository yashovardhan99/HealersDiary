package com.yashovardhan99.healersdiary.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.activities.MainActivity
import com.yashovardhan99.healersdiary.activities.PatientView
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*

class PatientAddPaymentDialog : DialogFragment(), View.OnClickListener, DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {

    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var calendar: Calendar


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_add_payment_dialog, container, false)

        dateText = rootView.findViewById(R.id.paymentDate)
        timeText = rootView.findViewById(R.id.paymentTime)
        calendar = Calendar.getInstance()
        dateText.text = DateFormat.getDateInstance().format(calendar.time)
        timeText.text = DateFormat.getTimeInstance().format(calendar.time)
        dateText.setOnClickListener(this)
        timeText.setOnClickListener(this)

        //firestore init
        val db = FirebaseFirestore.getInstance()
        val patient = db.collection("users")
                .document(FirebaseAuth.getInstance().uid!!)
                .collection("patients")
                .document(activity!!.intent.getStringExtra(MainActivity.PATIENT_UID))

        val save = rootView.findViewById<Button>(R.id.saveNewPayment)
        save.setOnClickListener(View.OnClickListener {
            //save new healing
            val amt = rootView.findViewById<TextInputEditText>(R.id.paymentReceived)
            val amount: Double
            try {
                amount = java.lang.Double.parseDouble(amt.text!!.toString())
            } catch (e: Exception) {
                amt.error = getString(R.string.enter_valid_amt)
                return@OnClickListener
            }

            patient.get()
                    .addOnCompleteListener { task ->
                        var due = 0.00
                        if (task.isSuccessful) {
                            if (task.result!!.data!!.containsKey("Due"))
                                due = java.lang.Double.parseDouble(task.result!!.data!!["Due"]!!.toString())
                            due -= amount
                            patient.update("Due", due)
                        }
                    }
            val payment = HashMap<String, Any>()
            payment["Date"] = calendar.time
            payment["Amount"] = amount
            patient.collection("payments")
                    .document(Calendar.getInstance().timeInMillis.toString())
                    .set(payment)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                            Log.d("FIRESTORE", "Payment record added")
                        else {
                            Log.d("FIRESTORE", "Payment record added error : " + task.exception!!.message)
                        }
                    }
            //amount added
            (activity as PatientView).paymentAdded(NumberFormat.getCurrencyInstance().format(amount))
            dismiss()
        })
        return rootView
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.paymentDate -> {
                val datePickerFragment = DatePickerFragment()
                val bundle = Bundle()
                bundle.putLong("DATE", calendar.timeInMillis)
                datePickerFragment.arguments = bundle
                datePickerFragment.setTargetFragment(this, 0)
                datePickerFragment.show(activity!!.supportFragmentManager, "datePicker")
            }
            R.id.paymentTime -> {
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
