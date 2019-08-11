package com.yashovardhan99.healersdiary.activities

import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.adapters.HealingLogAdapter
import com.yashovardhan99.healersdiary.objects.Healing
import java.text.DateFormat
import java.util.*

class HealingLogs : AppCompatActivity() {

    private lateinit var mAdapter: RecyclerView.Adapter<*>
    private lateinit var logs: CollectionReference
    //arraylist setup
    private val healings = ArrayList<Healing>()
    private lateinit var db: FirebaseFirestore
    private var thisDay: Int = 0
    private var thisMonth: Int = 0
    private var lastMonth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_healing_logs)

        //toolbar setup
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = intent.getStringExtra("PATIENT_NAME")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //setting counters
        thisDay = 0
        thisMonth = 0
        lastMonth = 0

        //initialize firestore
        db = FirebaseFirestore.getInstance()
        logs = db.collection(MainActivity.USERS)
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("patients")
                .document(intent.getStringExtra(MainActivity.PATIENT_UID))
                .collection("healings")


        //collection reference to all healing logs for this patient
        logs.limit(100)
                .orderBy("Date", Query.Direction.DESCENDING)
                .addSnapshotListener(EventListener { queryDocumentSnapshots, e ->
                    val df = DateFormat.getDateTimeInstance()
                    if (e != null) {
                        Log.d("FIRESTORE", e.localizedMessage)
                        return@EventListener
                    }
                    Log.d("FIRESTORE", "Logs fetched")

                    //fetching changes to this patient's healing collection
                    if (queryDocumentSnapshots != null) {
                        loop@ for (dc in queryDocumentSnapshots.documentChanges) {

                            val date = dc.document.getDate("Date")
                            val healing = Healing(df.format(date), dc.document.id)

                            //get today's date
                            val day = Calendar.getInstance()
                            day.time = Date()
                            val cal = Calendar.getInstance()
                            cal.time = date

                            val today = DateUtils.isToday(cal.timeInMillis)//check today
                            val month = cal.get(Calendar.MONTH) == day.get(Calendar.MONTH)//check this month
                            //for last month
                            cal.add(Calendar.MONTH, 1)
                            val last = cal.get(Calendar.MONTH) == day.get(Calendar.MONTH)

                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    //new healing added
                                    healings.add(healing)
                                    mAdapter.notifyItemInserted(healings.size - 1)
                                    //increment counters appropriately
                                    if (today)
                                        thisDay++
                                    if (month)
                                        thisMonth++
                                    if (last)
                                        lastMonth++
                                }
                                DocumentChange.Type.REMOVED -> {
                                    //healing removed
                                    //decrement counters
                                    if (today)
                                        thisDay--
                                    if (month)
                                        thisMonth--
                                    if (last)
                                        lastMonth--
                                    val pos = healings.indexOf(healing)
                                    if (pos >= 0) {
                                        healings.remove(healing)
                                        mAdapter.notifyItemRemoved(pos)
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> mAdapter.notifyItemChanged(healings.indexOf(healing))
                            }
                            //NOTE - healing data cannot be modified
                            updateTextFields()
                        }
                    }
                })

        //recycler view setup
        val mRecyclerView = findViewById<RecyclerView>(R.id.healingLogRecyclerView)
        mRecyclerView.setHasFixedSize(false)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = HealingLogAdapter(healings)
        mRecyclerView.adapter = mAdapter

        //for the divider lines between each record
        val itemDecoration = DividerItemDecoration(mRecyclerView.context, DividerItemDecoration.VERTICAL)
        mRecyclerView.addItemDecoration(itemDecoration)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            //this is to prevent empty activity from loading up on up button press which could lead to app crash
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.title.toString()) {
            "Delete" -> {
                //record to be deleted
                deleteHealing(item.groupId)//deleted from firestore
                healings.removeAt(item.groupId)//removed from list
                Snackbar.make(findViewById(R.id.healingLogRecyclerView), R.string.deleted, Snackbar.LENGTH_LONG)
                mAdapter.notifyItemRemoved(item.groupId)//updated adapter
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun deleteHealing(id: Int) {
        //deletes healing from firestore
        val (_, uid) = healings[id]
        logs.document(uid)
                .delete()
        //now changing amount due
        logs.parent!!.get().addOnCompleteListener { task ->
            var due = 0.00
            var rate = 0.00
            if (task.isSuccessful && task.result != null) {
                if (task.result!!.contains("Rate")) {
                    rate = task.result!!.getDouble("Rate")!!
                }
                if (task.result!!.contains("Due"))
                    due = task.result!!.getDouble("Due")!!
            } else
                Log.d("FIRESTORE", task.exception.toString())

            logs.parent!!.update("Due", due - rate)//updating amount due
        }
    }

    private fun updateTextFields() {
        val today = findViewById<TextView>(R.id.todayHealings)
        val month = findViewById<TextView>(R.id.thisMonthHealings)
        val last = findViewById<TextView>(R.id.lastMonthHealings)

        val res = resources

        today.text = res.getQuantityString(R.plurals.healing, thisDay, thisDay, getString(R.string.today))
        month.text = res.getQuantityString(R.plurals.healing, thisMonth, thisMonth, getString(R.string.this_month))
        last.text = res.getQuantityString(R.plurals.healing, lastMonth, lastMonth, getString(R.string.last_month))
    }
}