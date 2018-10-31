package com.yashovardhan99.healersdiary.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yashovardhan99.healersdiary.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewPatientFeedback extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient_feedback);

        //get Uids
        final String USERID = FirebaseAuth.getInstance().getUid();
        final String UID = getIntent().getStringExtra(MainActivity.PATIENT_UID);

        //initialize firestore
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        //get save button and edit text
        final EditText editText = findViewById(R.id.patientFeedbackEditText);
        Button save = findViewById(R.id.PatientFeedbackSaveBtn);

        //set save listener
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //first check if data isn't blank
                String feedback = editText.getText().toString();
                if(feedback.trim().isEmpty()){
                    //feedback empty text
                    editText.setError(getString(R.string.not_blank_error));
                    return;
                }

                //get current time
                Calendar calendar = Calendar.getInstance();

                Map<String, Object> feedbackObj = new HashMap<>();
                feedbackObj.put("Feedback",feedback);
                feedbackObj.put("Date",calendar.getTime());
                //save data

                //not empty
                final DocumentReference documentReference = firestore.collection(MainActivity.USERS)
                        .document(USERID)
                        .collection("patients")
                        .document(UID)
                        .collection("feedbacks")
                        .document(String.valueOf(calendar.getTimeInMillis()));
                //reference to new feedback

                //create doc
                documentReference.set(feedbackObj);

                //return success code
                setResult(RESULT_OK);
                finish();
            }
        });

    }
}
