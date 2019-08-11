package com.yashovardhan99.healersdiary.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.adapters.PatientPaymentLogsAdapter
import com.yashovardhan99.healersdiary.objects.PaymentSnapshot
import java.text.DateFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

class PatientPaymentLogs : AppCompatActivity() {

    private lateinit var mAdapter: RecyclerView.Adapter<*>
    private val payments = ArrayList<PaymentSnapshot>()
    private lateinit var logs: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_payment_logs)

        //toolbar setup
        setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.payment_logs)

        //fetching collection of payments
        logs = FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().uid!!)
                .collection("patients")
                .document(intent.getStringExtra(MainActivity.PATIENT_UID))
                .collection("payments")

        //fetching payment records here
        logs.orderBy("Date", Query.Direction.ASCENDING).addSnapshotListener { queryDocumentSnapshots, _ ->
            val df = DateFormat.getDateTimeInstance()
            for (dc in queryDocumentSnapshots!!.documentChanges) {
                val payment = PaymentSnapshot(dc.document.id,
                        df.format(dc.document.getDate("Date")),
                        NumberFormat.getCurrencyInstance().format(dc.document.getDouble("Amount")))

                when (dc.type) {

                    ADDED -> {
                        payments.add(0, payment)
                        mAdapter.notifyItemInserted(0)
                    }

                    REMOVED -> {
                        val pos = payments.indexOf(payment)
                        if (pos >= 0) {
                            payments.remove(payment)
                            mAdapter.notifyItemRemoved(pos)
                        }
                    }
                    MODIFIED -> mAdapter.notifyItemChanged(payments.indexOf(payment))
                }
            }
        }

        val mRecyclerView = findViewById<RecyclerView>(R.id.patientPaymentLogsRecycler)
        mRecyclerView.setHasFixedSize(false)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = PatientPaymentLogsAdapter(payments)
        mRecyclerView.adapter = mAdapter

        val itemDecoration = DividerItemDecoration(mRecyclerView.context, DividerItemDecoration.VERTICAL)
        mRecyclerView.addItemDecoration(itemDecoration)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val editItem = getString(R.string.edit)
        val deleteItem = getString(R.string.delete)
        if (item.title.toString() == editItem) {
            //edit payment
            Snackbar.make(findViewById(R.id.patientPaymentLogsRecycler), R.string.not_yet_implemented, Snackbar.LENGTH_LONG).show()
            Log.d("CONTEXT MENU", "EDIT - " + item.groupId)
            return true
        } else if (item.title.toString() == deleteItem) {
            //delete record
            Log.d("CONTEXT MENU", "DELETE - " + item.groupId)
            deletePayment(item.groupId)
            return true
        }
        return super.onContextItemSelected(item)
    }

    private fun deletePayment(id: Int) {
        //delete the payment record from firestore
        var amount = 0.00
        try {
            amount = NumberFormat.getCurrencyInstance().parse(payments[id].amount).toDouble()
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val finalAmount = amount
        logs.parent!!
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var due = 0.00
                        if (task.result!!.contains("Due"))
                            due = task.result!!.getDouble("Due")!!
                        due += finalAmount
                        logs.parent!!.update("Due", due)
                    }
                }
        logs.document(payments[id].uid).delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(MainActivity.FIRESTORE, "Deleted Payment successfully")
                    } else {
                        Log.d(MainActivity.FIRESTORE, "Error - " + task.exception!!)
                    }
                }
        Snackbar.make(findViewById(R.id.patientPaymentLogsRecycler), "Payment Deleted : " + NumberFormat.getCurrencyInstance().format(finalAmount), Snackbar.LENGTH_LONG).show()
        payments.removeAt(id)
        mAdapter.notifyItemRemoved(id)
    }
}
