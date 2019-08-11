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
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.adapters.PatientFeedbackListAdapter
import com.yashovardhan99.healersdiary.objects.PatientFeedback
import java.util.*

class PatientFeedbackLogs : AppCompatActivity() {

    private lateinit var mAdapter: RecyclerView.Adapter<*>
    private val feedbacks = ArrayList<PatientFeedback>()
    private lateinit var logs: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_feedback_logs)

        //toolbar setup
        setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.patient_feedbacks)

        //fetching collection of feedbacks
        logs = FirebaseFirestore.getInstance()
                .collection(MainActivity.USERS)
                .document(FirebaseAuth.getInstance().uid!!)
                .collection("patients")
                .document(intent.getStringExtra(MainActivity.PATIENT_UID))
                .collection("feedbacks")

        //fetching records
        logs.orderBy("Date", Query.Direction.DESCENDING).limit(100)
                .addSnapshotListener { queryDocumentSnapshots, _ ->
                    for (dc in queryDocumentSnapshots!!.documentChanges) {
                        val feedback = PatientFeedback(dc.document.id,
                                dc.document.getString("Feedback")!!,
                                dc.document.getTimestamp("Date")!!,
                                dc.document.contains("Verified"))

                        when (dc.type) {

                            DocumentChange.Type.ADDED -> {
                                feedbacks.add(feedback)
                                mAdapter.notifyItemInserted(feedbacks.size - 1)
                            }

                            DocumentChange.Type.MODIFIED -> {
                                for (patientFeedback in feedbacks) {
                                    if (patientFeedback.uid == feedback.uid) {
                                        val index = feedbacks.indexOf(patientFeedback)
                                        feedbacks[index] = feedback
                                        mAdapter.notifyItemChanged(index)
                                        break
                                    }
                                }
                                val pos = feedbacks.indexOf(feedback)
                                if (pos >= 0) {
                                    feedbacks.remove(feedback)
                                    mAdapter.notifyItemRemoved(pos)
                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                val pos = feedbacks.indexOf(feedback)
                                if (pos >= 0) {
                                    feedbacks.remove(feedback)
                                    mAdapter.notifyItemRemoved(pos)
                                }
                            }
                        }
                    }
                }

        //assign recycler view and adapters
        val mRecyclerView = findViewById<RecyclerView>(R.id.PatientFeedbackLogsRecycler)
        mRecyclerView.setHasFixedSize(false)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = PatientFeedbackListAdapter(feedbacks)
        mRecyclerView.adapter = mAdapter

        //adds a line decor after each item
        val itemDecoration = DividerItemDecoration(mRecyclerView.context, DividerItemDecoration.VERTICAL)
        mRecyclerView.addItemDecoration(itemDecoration)
    }

    //to handle up button press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Edit or delete records
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val editItem = getString(R.string.edit)
        val deleteItem = getString(R.string.delete)
        if (item.title.toString() == editItem) {
            //edit payment
            Snackbar.make(findViewById(R.id.PatientFeedbackLogsRecycler), R.string.not_yet_implemented, Snackbar.LENGTH_LONG).show()
            Log.d("CONTEXT MENU", "EDIT - " + item.groupId)
            return true
        } else if (item.title.toString() == deleteItem) {
            //delete record
            Log.d("CONTEXT MENU", "DELETE - " + item.groupId)
            deleteFeedback(item.groupId)
            return true
        }
        return super.onContextItemSelected(item)
    }

    private fun deleteFeedback(id: Int) {
        logs.document(feedbacks[id].uid).delete()
        Snackbar.make(findViewById(R.id.PatientFeedbackLogsRecycler), R.string.deleted, Snackbar.LENGTH_SHORT).show()
        feedbacks.removeAt(id)
        mAdapter.notifyItemRemoved(id)
    }
}
