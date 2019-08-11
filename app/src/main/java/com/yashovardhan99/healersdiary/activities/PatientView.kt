package com.yashovardhan99.healersdiary.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.fragments.GeneratePatientBill
import com.yashovardhan99.healersdiary.fragments.NewHealingDialog
import com.yashovardhan99.healersdiary.fragments.PatientAddPaymentDialog
import com.yashovardhan99.healersdiary.helpers.HtmlCompat
import java.text.NumberFormat

class PatientView : AppCompatActivity(), View.OnClickListener {

    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_view)

        setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Patient Detail"
        //toolbar setup

        uid = intent.getStringExtra(MainActivity.PATIENT_UID)
        //patient record UID to fetch records from firestore

        val db = FirebaseFirestore.getInstance()
        //initialize firestore
        val documentReference = db.collection(MainActivity.USERS)
                .document(FirebaseAuth.getInstance().uid!!)
                .collection("patients")
                .document(uid)
        //create ref to patient document
        documentReference.addSnapshotListener(EventListener { document, _ ->
            //listener to update patient data
            if (document != null && document.exists()) {
                //get document data
                Log.d(MainActivity.FIRESTORE, "Data exists : " + document.data!!)
                //store it in a map and update relevant fields
                val patient = document.data ?: return@EventListener//data extracted

                val name = findViewById<TextView>(R.id.patientNameInDetail)
                if (patient.containsKey("Name"))
                    name.text = patient["Name"]!!.toString()

                val disease = findViewById<TextView>(R.id.patientDiseaseInDetail)
                if (patient.containsKey("Disease") && patient["Disease"]!!.toString().isNotEmpty())
                    disease.text = patient["Disease"]!!.toString()
                else
                    disease.visibility = View.GONE

                val due = findViewById<TextView>(R.id.bill)
                if (patient.containsKey("Due") && patient["Due"]!!.toString().isNotEmpty()) {
                    val amt = java.lang.Double.parseDouble(patient["Due"]!!.toString())
                    val famt = getString(R.string.payment_due) + ": <b><big>" + NumberFormat.getCurrencyInstance().format(amt) + "</big></b>"
                    due.text = HtmlCompat.fromHtml(famt)
                } else
                    due.visibility = View.GONE
            }
        })

        //new healing button - to add new healing
        val newHealing = findViewById<Button>(R.id.newHealingButton)
        newHealing.setOnClickListener(this)

        //go to healing logs
        val healingLogs = findViewById<Button>(R.id.healingLogsButton)
        healingLogs.setOnClickListener {
            val logs = Intent(this@PatientView, HealingLogs::class.java)
            logs.putExtra("PATIENT_NAME", (findViewById<View>(R.id.patientNameInDetail) as TextView).text)
            logs.putExtra(MainActivity.PATIENT_UID, uid)
            startActivity(logs)
        }

        //add new payment
        val addPaymentButton = findViewById<Button>(R.id.enterNewPPayment)
        addPaymentButton.setOnClickListener {
            val addPayment = PatientAddPaymentDialog()
            addPayment.show(supportFragmentManager, "NewPayment")
        }

        //go to payment logs
        val paymentLogs = findViewById<Button>(R.id.viewPatientPaymentLogs)
        paymentLogs.setOnClickListener {
            val logs = Intent(this@PatientView, PatientPaymentLogs::class.java)
            logs.putExtra(MainActivity.PATIENT_UID, uid)
            startActivity(logs)
        }

        //add new patient feedback
        val newFeedback = findViewById<Button>(R.id.newFeedbackButton)
        newFeedback.setOnClickListener {
            val newFeed = Intent(this@PatientView, NewPatientFeedback::class.java)
            newFeed.putExtra(MainActivity.PATIENT_UID, uid)
            startActivityForResult(newFeed, REQUEST_FEEDBACK_ADDED)
        }

        //go to feedback logs
        val feedbackLogs = findViewById<Button>(R.id.patientFeedbackLogs)
        feedbackLogs.setOnClickListener {
            val logs = Intent(this@PatientView, PatientFeedbackLogs::class.java)
            logs.putExtra(MainActivity.PATIENT_UID, uid)
            startActivity(logs)
        }

        val bill = findViewById<Button>(R.id.generateBill)
        bill.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {

            REQUEST_FEEDBACK_ADDED -> if (resultCode == Activity.RESULT_OK)
                Snackbar.make(findViewById(R.id.patientNameInDetail), R.string.added, Snackbar.LENGTH_SHORT).show()
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun paymentAdded(amt: String) {
        Snackbar.make(findViewById(R.id.patientNameInDetail), getString(R.string.payment_added, amt), Snackbar.LENGTH_LONG).show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.newHealingButton -> {
                val newHealing = NewHealingDialog()
                newHealing.show(supportFragmentManager, "HEALINGNEW")
            }
            R.id.generateBill -> {
                val generateBill = GeneratePatientBill()
                val bundle = Bundle()
                bundle.putString(MainActivity.PATIENT_UID, uid)
                generateBill.arguments = bundle
                generateBill.show(supportFragmentManager, "generatebill")
            }
        }
    }

    companion object {
        private const val REQUEST_FEEDBACK_ADDED = 2
    }
}
