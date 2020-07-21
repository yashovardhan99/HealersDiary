package com.yashovardhan99.healersdiary.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.adapters.MainListAdapter
import com.yashovardhan99.healersdiary.fragments.AboutFragment
import com.yashovardhan99.healersdiary.fragments.MainListFragment
import com.yashovardhan99.healersdiary.fragments.SettingsFragment
import com.yashovardhan99.healersdiary.fragments.SignOutFragment
import com.yashovardhan99.healersdiary.objects.Patient
import io.fabric.sdk.android.Fabric
import java.util.*
import kotlin.math.max

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var patientList: ArrayList<Patient>
    private lateinit var queryDocumentSnapshots: EventListener<QuerySnapshot>
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var listFragment: MainListFragment
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var aboutFragment: AboutFragment
    private lateinit var mContent: Fragment
    lateinit var adapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        resetHealingCounters()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDrawerLayout = findViewById(R.id.drawer_layout)
        val mNavigationView = findViewById<NavigationView>(R.id.main_nav_view)

        val profilePic = mNavigationView.getHeaderView(0).findViewById<ImageView>(R.id.profilePic)
        val profileName = mNavigationView.getHeaderView(0).findViewById<TextView>(R.id.profileName)

        //check login and handle
        mAuth = FirebaseAuth.getInstance()
        val mUser = mAuth.currentUser

        listFragment = MainListFragment()
        aboutFragment = AboutFragment()
        settingsFragment = SettingsFragment()

        if (savedInstanceState != null) {
            mContent = supportFragmentManager.getFragment(savedInstanceState, FRAG_KEY)!!
            supportFragmentManager.beginTransaction().replace(R.id.mainListHolder, mContent).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.mainListHolder, listFragment).commit()
            mContent = listFragment
        }


        val profilePicture = mUser?.photoUrl

        Picasso.get().load(profilePicture).placeholder(R.drawable.ic_person_black_24dp).centerCrop().fit().into(profilePic, object : Callback {
            override fun onSuccess() {
                val imageBit = (profilePic.drawable as BitmapDrawable).bitmap
                val bitmapDrawable = RoundedBitmapDrawableFactory.create(resources, imageBit)
                bitmapDrawable.isCircular = true
                bitmapDrawable.cornerRadius = max(imageBit.width, imageBit.height) / 2.0f
                profilePic.setImageDrawable(bitmapDrawable)
            }

            override fun onError(e: Exception) {}
        })


        mNavigationView.setNavigationItemSelectedListener(this)

        //display welcome message
        profileName.text = mUser?.displayName

        //firestore init
        db = FirebaseFirestore.getInstance()


        //load data from database

        patientList = ArrayList()

        val patients = db.collection(USERS)
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("patients")

        //to instantly make any changes reflect here
        queryDocumentSnapshots = EventListener { queryDocumentSnapshots, e ->
            if (e != null) {
                Log.d(FIRESTORE, "ERROR : " + e.message)
                return@EventListener
            }
            Log.d(FIRESTORE, "Data fetched")
            if (queryDocumentSnapshots == null)
                return@EventListener
            for (dc in queryDocumentSnapshots.documentChanges) {
                //getting changes in documents
                Log.d(FIRESTORE, dc.document.data.toString())

                val document = dc.document

                when (dc.type) {

                    DocumentChange.Type.ADDED -> {
                        //add new patient to arrayList
                        val patient = Patient()
                        patient.name = Objects.requireNonNull(document.get("Name")).toString()
                        patient.uid = document.id
                        patient.disease = Objects.requireNonNull(document.get("Disease")).toString()
                        if (document.contains("Due") && document.get("Due")!!.toString().isNotEmpty()) {
                            patient.due = document.getDouble("Due")!!
                        }
                        if (document.contains("Rate") && document.get("Rate")!!.toString().isNotEmpty()) {
                            patient.rate = document.getDouble("Rate")!!
                        }
                        patientList.add(patient)
                        adapter.notifyItemInserted(patientList.indexOf(patient))
                        countHealings(patient)
                    }

                    DocumentChange.Type.MODIFIED -> {
                        //modify patient name
                        val id = dc.document.id
                        for (patient1 in patientList) {
                            if (patient1.uid == id) {
                                patient1.name = document.get("Name").toString()
                                patient1.disease = document.get("Disease").toString()
                                if (document.contains("Due") && document.get("Due").toString().isNotEmpty()) {
                                    patient1.due = document.getDouble("Due")!!
                                }
                                if (document.contains("Rate") && document.get("Rate").toString().isNotEmpty()) {
                                    patient1.rate = document.getDouble("Rate")!!
                                }
                                adapter.notifyItemChanged(patientList.indexOf(patient1))
                                break
                            }
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        //remove patient record
                        val id2 = dc.document.id
                        for (patient1 in patientList) {
                            if (patient1.uid == id2) {
                                val pos = patientList.indexOf(patient1)
                                patientList.remove(patient1)
                                adapter.notifyItemRemoved(pos)
                                break
                            }
                        }
                    }
                }
            }
        }

        patients.addSnapshotListener(queryDocumentSnapshots)
        adapter = MainListAdapter(patientList, getPreferences(Context.MODE_PRIVATE))
        if (getPreferences(Context.MODE_PRIVATE).getBoolean(CRASH_ENABLED, true))
            this.setCrashEnabled()
    }

    private fun resetHealingCounters() {
        //initialize healings counter
        healingsToday = 0
        healingsYesterday = 0
    }

    private fun signOut() {
        val signOutFragment = SignOutFragment()
        signOutFragment.show(supportFragmentManager, "SIGNOUT")
        //        startActivity(new Intent(MainActivity.this, Login.class)
        //                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
        //        finishAffinity();
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        val title = item.title.toString()
        val edit = getString(R.string.edit)
        val delete = getString(R.string.delete)
        when (title) {
            edit -> {
                //edit patient data
                val editPatient = Intent(this, NewPatient::class.java)
                editPatient.putExtra("EDIT", true)
                val id = patientList[item.groupId].uid
                editPatient.putExtra(PATIENT_UID, id)
                startActivity(editPatient)

                //log analytics
                val editBundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.LOCATION, MainActivity::class.java.name)
                    putString(FirebaseAnalytics.Param.CONTENT_TYPE, EDIT_BUTTON)
                    putString(FirebaseAnalytics.Param.ITEM_CATEGORY, PATIENT_RECORD)
                    putString(FirebaseAnalytics.Param.ITEM_ID, id)
                }
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, editBundle)

                return true
            }
            delete -> {
                //delete patient data
                val confirmBuilder = AlertDialog.Builder(this@MainActivity)
                confirmBuilder.setMessage(R.string.delete_warning_message)
                        .setTitle(R.string.sure_q)
                        .setPositiveButton(R.string.delete) { _, _ -> deletePatientRecord(item.groupId) }
                        .setNegativeButton(R.string.cancel) { _, _ ->
                            //action cancelled
                        }
                //to confirm deletion
                val confirm = confirmBuilder.create()
                confirm.show()
                return true
            }
            else -> return super.onContextItemSelected(item)
        }
    }

    private fun deletePatientRecord(id: Int) {
        val patient = db.collection(USERS)
                .document(mAuth.uid!!)
                .collection("patients")
                .document(patientList[id].uid)
        //now to delete this record, we first delete all healing and payment history of this patient
        val healings = patient
                .collection("healings")
        healings.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (documentSnapshot in task.result!!) {
                    documentSnapshot.reference.delete()
                }
            }
        }

        val payments = patient.collection("payments")
        payments.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (documentSnapshot in task.result!!) {
                    documentSnapshot.reference.delete()
                }
            }
        }

        //now deleting patient document
        patient.delete()
        Snackbar.make(findViewById(R.id.recycler_main), R.string.deleted, Snackbar.LENGTH_LONG).show()

        //logging in analytics
        val delete = Bundle()
        delete.putString(FirebaseAnalytics.Param.CONTENT_TYPE, DELETE_BUTTON)
        delete.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, PATIENT_RECORD)
        delete.putString(FirebaseAnalytics.Param.LOCATION, MainActivity::class.java.name)
        delete.putString(FirebaseAnalytics.Param.ITEM_ID, patient.id)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, delete)
    }

    private fun countHealings(patient: Patient) {
        Log.d("COUNTING HEALINGS", patient.uid)

        //yesterday's timestamp
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -2)
        val yest = yesterday.time
        val y = Timestamp(yest)

        db.collection(USERS)
                .document(mAuth.uid!!)
                .collection("patients")
                .document(patient.uid)
                .collection("healings")
                .orderBy("Date", Query.Direction.DESCENDING)
                .whereGreaterThan("Date", y)
                .addSnapshotListener(EventListener { queryDocumentSnapshots, _ ->
                    if (queryDocumentSnapshots == null)
                        return@EventListener
                    for (dc in queryDocumentSnapshots.documentChanges) {
                        val timestamp = dc.document.getTimestamp("Date") ?: continue
                        Log.d("COUNTING HEALINGS", timestamp.toString())
                        val time = timestamp.toDate().time
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> if (DateUtils.isToday(time)) {
                                healingsToday++
                                patient.healingsToday = patient.healingsToday + 1
                            } else if (DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS))
                                healingsYesterday++
                            DocumentChange.Type.REMOVED -> if (DateUtils.isToday(time)) {
                                healingsToday--
                                patient.healingsToday = patient.healingsToday - 1
                            } else if (DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS))
                                healingsYesterday--
                            else -> adapter.notifyItemChanged(patientList.indexOf(patient))
                        }
                    }
                    adapter.notifyItemChanged(patientList.indexOf(patient))
                    try {
                        listFragment.updateTextFields()
                    } catch (ise: IllegalStateException) {
                        Log.d("COUNTING HEALINGS", "UDT NOT CALLED$ise")
                    }
                })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            R.id.shareMenuItem -> {
                share()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun share() {
        val share = Intent(Intent.ACTION_SEND)
        share.putExtra(Intent.EXTRA_TEXT, "Healers Diary - A simple and free app to keep track of your patients, healings, payments and more! Download now at https://play.google.com/store/apps/details?id=com.yashovardhan99.healersdiary")
        share.type = "text/plain"
        startActivity(Intent.createChooser(share, getString(R.string.share)))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_options_menu, menu)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState, FRAG_KEY, mContent)
    }

    fun setCrashEnabled() {
        Log.d("CRASH", "Enabled")
        Fabric.with(this, Crashlytics())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.groupId) {
            R.id.MainNavGroup -> {
                item.isChecked = true
                mDrawerLayout.closeDrawers()
                mContent = when (item.itemId) {
                    R.id.home -> listFragment
                    R.id.about -> aboutFragment
                    R.id.settings -> settingsFragment
                    else -> return false
                }
                val transaction = supportFragmentManager.beginTransaction().replace(R.id.mainListHolder, mContent)
                if (mContent === listFragment)
                    clearBackStack()
                else
                    transaction.addToBackStack(mContent.javaClass.name)
                transaction.commit()
                return true
            }
            R.id.extra_nav_group -> when (item.itemId) {
                R.id.signOutMenuItem -> {
                    signOut()
                    return true
                }
            }
        }
        return false
    }

    private fun clearBackStack() {
        val fm = supportFragmentManager
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    companion object {

        const val PATIENT_UID = "PATIENT_UID"
        const val BILL_TYPE = "billtype"
        const val PATIENT_RECORD = "patient_record"
        const val NEW = "New"
        const val MAIN_LIST_CHOICE = "MAIN_LIST_CHOICE"
        const val CRASH_ENABLED = "CRASH_REPORTING_STATE"
        const val USERS = "users"
        const val FIRESTORE = "FIRESTORE"
        const val INCLUDE_LOGS = "includeHealingLogs"
        private const val DELETE_BUTTON = "Delete Button"
        private const val EDIT_BUTTON = "Edit Button"
        lateinit var mFirebaseAnalytics: FirebaseAnalytics
        var healingsToday: Int = 0
        var healingsYesterday: Int = 0
        private const val FRAG_KEY = "MY_FRAG"
    }
}