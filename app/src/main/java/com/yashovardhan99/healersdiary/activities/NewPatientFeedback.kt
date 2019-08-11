package com.yashovardhan99.healersdiary.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yashovardhan99.healersdiary.R
import java.util.*

class NewPatientFeedback : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_patient_feedback)

        //get Uids
        val userId = FirebaseAuth.getInstance().uid
        val uid = intent.getStringExtra(MainActivity.PATIENT_UID)

        //initialize firestore
        val firestore = FirebaseFirestore.getInstance()

        //get save button and edit text
        val editText = findViewById<EditText>(R.id.patientFeedbackEditText)
        val save = findViewById<Button>(R.id.PatientFeedbackSaveBtn)

        //set save listener
        save.setOnClickListener(View.OnClickListener {
            //first check if data isn't blank
            val feedback = editText.text.toString()
            if (feedback.trim { it <= ' ' }.isEmpty()) {
                //feedback empty text
                editText.error = getString(R.string.not_blank_error)
                return@OnClickListener
            }

            //get current time
            val calendar = Calendar.getInstance()

            val feedbackObj = HashMap<String, Any>()
            feedbackObj["Feedback"] = feedback
            feedbackObj["Date"] = calendar.time
            //save data

            //not empty
            val documentReference = firestore.collection(MainActivity.USERS)
                    .document(userId!!)
                    .collection("patients")
                    .document(uid)
                    .collection("feedbacks")
                    .document(calendar.timeInMillis.toString())
            //reference to new feedback

            //create doc
            documentReference.set(feedbackObj)

            //return success code
            setResult(Activity.RESULT_OK)
            finish()
        })

    }
}
