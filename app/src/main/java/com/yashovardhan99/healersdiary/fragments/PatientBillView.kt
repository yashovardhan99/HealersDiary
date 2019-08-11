package com.yashovardhan99.healersdiary.fragments


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.activities.MainActivity
import com.yashovardhan99.healersdiary.objects.Patient
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class PatientBillView : DialogFragment(), com.google.firebase.firestore.EventListener<DocumentSnapshot>, View.OnClickListener {

    private var billtype: Int = 0
    private var detailed: Boolean = false
    private lateinit var uid: String
    private lateinit var patient: Patient
    private lateinit var billText: TextView
    private lateinit var documentReference: DocumentReference
    private lateinit var registration: ListenerRegistration
    @SuppressLint("SetTextI18n")
    private var healingListener: com.google.firebase.firestore.EventListener<QuerySnapshot>? = com.google.firebase.firestore.EventListener { queryDocumentSnapshots, _ ->
        var no = 0
        assert(queryDocumentSnapshots != null)
        for (documentSnapshot in queryDocumentSnapshots!!) {
            no++
            if (detailed) {
                val date = DateFormat.getDateTimeInstance().format(documentSnapshot.getDate("Date"))
                billText.text = billText.text.toString() + "\n" + date
            }
        }
        patient.due = patient.rate * no
        billText.text = billText.text.toString() + "\n" + getString(R.string.bill,
                patient.name,
                DateFormat.getDateInstance().format(Calendar.getInstance().time),
                NumberFormat.getCurrencyInstance().format(patient.due),
                FirebaseAuth.getInstance().currentUser!!.displayName)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_patient_bill_view, container, false)
        billText = rootView.findViewById(R.id.billText)
        val share = rootView.findViewById<Button>(R.id.shareBill)
        val close = rootView.findViewById<Button>(R.id.closeDialog)
        close.setOnClickListener(this)
        share.setOnClickListener(this)
        val bundle = arguments
        uid = bundle!!.getString(MainActivity.PATIENT_UID)!!
        billtype = bundle.getInt(MainActivity.BILL_TYPE)
        val db = FirebaseFirestore.getInstance()
        documentReference = db.collection(MainActivity.USERS)
                .document(FirebaseAuth.getInstance().uid!!)
                .collection("patients")
                .document(uid)
        registration = documentReference.addSnapshotListener(this)
        patient = Patient()
        patient.uid = uid
        detailed = bundle.getBoolean(MainActivity.INCLUDE_LOGS)
        return rootView
    }

    override fun onEvent(documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        assert(documentSnapshot != null)
        patient.name = documentSnapshot!!.getString("Name")!!
        patient.due = java.lang.Double.parseDouble(documentSnapshot.get("Due")!!.toString())
        patient.rate = java.lang.Double.parseDouble(documentSnapshot.get("Rate")!!.toString())
        val thisMonth = Calendar.getInstance()
        thisMonth.set(Calendar.HOUR_OF_DAY, 0)
        thisMonth.clear(Calendar.MINUTE)
        thisMonth.clear(Calendar.SECOND)
        thisMonth.clear(Calendar.MILLISECOND)
        thisMonth.set(Calendar.DAY_OF_MONTH, 1)
        when (billtype) {
            R.id.allBillButton -> billText.text = getString(R.string.bill, patient.name,
                    DateFormat.getDateInstance().format(Calendar.getInstance().time),
                    NumberFormat.getCurrencyInstance().format(patient.due),
                    FirebaseAuth.getInstance().currentUser?.displayName)
            R.id.thisMonthBillButton -> {
                registration.remove()
                registration = documentReference.collection("healings").orderBy("Date", Query.Direction.ASCENDING)
                        .whereGreaterThanOrEqualTo("Date", Timestamp(thisMonth.time))
                        .addSnapshotListener(healingListener!!)
            }
            R.id.lastMonthBillButton -> {
                val lastMonth = Calendar.getInstance()
                lastMonth.add(Calendar.MONTH, -1)
                lastMonth.set(Calendar.DATE, 1)
                lastMonth.set(Calendar.HOUR_OF_DAY, 0)
                lastMonth.set(Calendar.MINUTE, 0)
                lastMonth.set(Calendar.SECOND, 0)
                lastMonth.set(Calendar.MILLISECOND, 0)
                Log.d("THISMONTH", thisMonth.time.toString())
                Log.d("LASTMONTH", lastMonth.time.toString())
                registration.remove()
                registration = documentReference.collection("healings")
                        .whereGreaterThanOrEqualTo("Date", Timestamp(lastMonth.time))
                        .whereLessThan("Date", Timestamp(thisMonth.time))
                        .orderBy("Date", Query.Direction.ASCENDING)
                        .addSnapshotListener(healingListener!!)
            }
        }
    }

    override fun onDetach() {
        registration.remove()
        healingListener = null
        super.onDetach()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.shareBill -> {
                billText.isSelected = true
                val text = billText.text.toString()
                val share = Intent(Intent.ACTION_SEND)
                share.putExtra(Intent.EXTRA_TEXT, text)
                share.type = "text/plain"
                startActivity(Intent.createChooser(share, "Send Bill"))
            }
            R.id.closeDialog -> dismiss()
        }
    }
}// Required empty public constructor
