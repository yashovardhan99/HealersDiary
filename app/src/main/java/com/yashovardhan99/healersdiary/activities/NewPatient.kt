package com.yashovardhan99.healersdiary.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.yashovardhan99.healersdiary.R
import java.text.NumberFormat
import java.util.*

class NewPatient : AppCompatActivity() {

    private lateinit var patientNameEditText: TextInputEditText
    private lateinit var contactNumberEditText: TextInputEditText
    private lateinit var rate: TextInputEditText
    private lateinit var disease: TextInputEditText
    private lateinit var due: TextInputEditText
    private lateinit var newPatientToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_patient)

        newPatientToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(newPatientToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.add_new_patient)
        //toolbar setup

        patientNameEditText = findViewById(R.id.patientName)
        contactNumberEditText = findViewById(R.id.phoneNumber)
        rate = findViewById(R.id.rate)
        due = findViewById(R.id.EnterPaymentDue)
        disease = findViewById(R.id.patientDisease)

        //Contact Picker Button
        val contactBrowse = findViewById<Button>(R.id.contact_picker_button)
        contactBrowse.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@NewPatient, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                //permission to read contacts not granted
                Log.d("CONTACT PERMISSION", "NOT GRANTED")
                ActivityCompat.requestPermissions(this@NewPatient, arrayOf(Manifest.permission.READ_CONTACTS),
                        READ_CONTACT_PERMISSION_REQUEST_CODE)//Requesting contact permission
            } else {
                //permission is there
                //call method for choosing contact
                Log.d("CONTACT PERMISSION", "GRANTED ALREADY")
                launchContactPicker()
            }
        }

        //add the currency symbol to the rate field
        rate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isNotEmpty() && !s.toString().startsWith(NumberFormat.getCurrencyInstance().currency.symbol)) {
                    val amount = NumberFormat.getCurrencyInstance().currency.symbol + s.toString()
                    rate.setText(amount)
                    rate.setSelection(amount.length)
                }
            }
        })

        //add the currency symbol to the due field
        due.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isNotEmpty() && !s.toString().startsWith(NumberFormat.getCurrencyInstance().currency.symbol)) {
                    val amount = NumberFormat.getCurrencyInstance().currency.symbol + s.toString()
                    due.setText(amount)
                    due.setSelection(amount.length)
                }
            }
        })

        //now saving
        val save = findViewById<Button>(R.id.saveNewPatient)

        Log.d("EDIT", intent.getBooleanExtra("EDIT", false).toString())

        //load previous content if editing
        if (intent.getBooleanExtra("EDIT", false)) {
            val id = intent.getStringExtra(MainActivity.PATIENT_UID)
            Log.d("EDIT", id)
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                    .document(FirebaseAuth.getInstance().uid!!)
                    .collection("patients")
                    .document(id)
                    .addSnapshotListener(EventListener { documentSnapshot, _ ->
                        if (!documentSnapshot!!.exists()) {
                            Snackbar.make(save, "Something went wrong!", Snackbar.LENGTH_INDEFINITE).show()
                            return@EventListener
                        }
                        if (documentSnapshot.contains("Name"))
                            patientNameEditText.setText(documentSnapshot.get("Name")!!.toString())
                        if (documentSnapshot.contains("Phone"))
                            contactNumberEditText.setText(documentSnapshot.get("Phone")!!.toString())
                        if (documentSnapshot.contains("Disease"))
                            disease.setText(documentSnapshot.get("Disease")!!.toString())
                        if (documentSnapshot.contains("Rate"))
                            rate.setText(documentSnapshot.get("Rate")!!.toString())
                        if (documentSnapshot.contains("Due"))
                            due.setText(documentSnapshot.get("Due")!!.toString())
                        save.setText(R.string.update)
                    })
        }

        save.setOnClickListener(View.OnClickListener {
            //error checking data
            if (patientNameEditText.text!!.toString().isEmpty()) {
                patientNameEditText.error = getString(R.string.name_cannot_be_blank)
                return@OnClickListener
            }
            //firestore
            val db = FirebaseFirestore.getInstance()
            //creating map of all data
            val patient = HashMap<String, Any>()
            patient["Name"] = patientNameEditText.text!!.toString()
            patient["Phone"] = contactNumberEditText.text!!.toString()
            patient["Disease"] = disease.text!!.toString()
            if (rate.text!!.toString().isNotEmpty())
                try {
                    patient["Rate"] = java.lang.Double.parseDouble(rate.text!!.toString().substring(1))
                } catch (e: NumberFormatException) {
                    rate.error = "Invalid Format"
                    return@OnClickListener
                }

            if (due.text!!.toString().isNotEmpty())
                try {
                    patient["Due"] = java.lang.Double.parseDouble(due.text!!.toString().substring(1))
                } catch (e: NumberFormatException) {
                    due.error = "Invalid format"
                    return@OnClickListener
                }

            patient["Date"] = Calendar.getInstance().time

            //creating docref for new or edited patient record
            val documentReference: DocumentReference
            documentReference = if (intent.getBooleanExtra("EDIT", false))
                db.collection("users")
                        .document(FirebaseAuth.getInstance().uid!!)
                        .collection("patients")
                        .document(intent.getStringExtra(MainActivity.PATIENT_UID))
            else
                db.collection("users")
                        .document(FirebaseAuth.getInstance().uid!!)
                        .collection("patients")
                        .document(Calendar.getInstance().timeInMillis.toString())

            //adding data to document
            documentReference.set(patient)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FIRESTORE", "Saved patient data")
                        } else {
                            Log.d("FIRESTORE", "Error : " + task.exception!!.message)
                        }
                    }
            //finally opening the relevant patient detail view
            val openPatientDetail = Intent(this@NewPatient, PatientView::class.java)
            openPatientDetail.putExtra(MainActivity.PATIENT_UID, documentReference.id)
            Log.d("PATIENT UID", documentReference.id)
            startActivity(openPatientDetail)
            finish()
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            READ_CONTACT_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission to read contacts granted
                Log.d("CONTACT PERMISSION", "PERMISSION GRANTED!")
                launchContactPicker()
            } else {
                //permission not granted... disable feature and show prompt
                Log.d("CONTACT PERMISSION", "PERMISSION DENIED!")
            }
        }
    }

    private fun launchContactPicker() {
        val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(pickContactIntent, CONTACT_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CONTACT_PICKER_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                //when we get the contact. to get the contact name and number
                Log.d("CONTACT PICKED", "RESULT OK")
                //result successful
                val contactData = data!!.data
                val c = contentResolver.query(contactData!!, null, null, null, null)
                if (c != null && c.moveToFirst()) {

                    val id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    Log.d("CONTACT PICKED", "ID = $id")
                    val hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    Log.d("CONTACT PICKED", name)
                    patientNameEditText.setText(name)
                    if (hasPhone == "1") {
                        Log.d("CONTACT PICKED", hasPhone)
                        val phones = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null)
                        phones!!.moveToFirst()
                        val phNo = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        Log.d("CONTACT PICKED", phNo)
                        contactNumberEditText.setText(phNo.trim { it <= ' ' })
                        c.close()
                        phones.close()
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val READ_CONTACT_PERMISSION_REQUEST_CODE = 1
        private const val CONTACT_PICKER_REQUEST_CODE = 2
    }
}
