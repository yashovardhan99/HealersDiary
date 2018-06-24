package com.yashovardhan99.healersdiary.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.yashovardhan99.healersdiary.R;

import java.text.NumberFormat;
import java.util.Map;

import javax.annotation.Nullable;

public class PatientView extends AppCompatActivity {

    public String Uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_view);

        setSupportActionBar((Toolbar) findViewById(R.id.patientViewToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar setup

        Uid = getIntent().getStringExtra("PATIENT_UID");
        //patient record UID to fetch records from firestore
        if (Uid != null) {

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            //initialize firestore
            final DocumentReference documentReference = db.collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("patients")
                    .document(Uid);
            //create ref to patient document
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                //listener to update patient data
                @Override
                public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException e) {
                    if (document!=null && document.exists()) {
                        //get document data
                        Log.d("FIRESTORE", "Data exists : " + document.getData());
                        //store it in a map and update relevant fields
                        Map<String, Object> patient = document.getData();//data extracted

                        if (patient == null)
                            return;

                        TextView name = findViewById(R.id.patientNameInDetail);
                        if (patient.containsKey("Name"))
                            name.setText(patient.get("Name").toString());

                        TextView disease = findViewById(R.id.patientDiseaseInDetail);
                        if (patient.containsKey("Disease") && !patient.get("Disease").toString().isEmpty())
                            disease.setText(patient.get("Disease").toString());
                        else
                            disease.setVisibility(View.GONE);

                        TextView due = findViewById(R.id.bill);
                        if (patient.containsKey("Due") && !patient.get("Due").toString().isEmpty()) {
                            Double amt = Double.parseDouble(patient.get("Due").toString());
                            String famt = "Amount Due : " + NumberFormat.getCurrencyInstance().format(amt);
                            due.setText(famt);
                        }
                        else
                            due.setVisibility(View.GONE);
                    }
                }

            });
        }

        Button newHealing = findViewById(R.id.newHealingButton);
        newHealing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newHealingIntent = new Intent(PatientView.this,NewHealingRecord.class);
                newHealingIntent.putExtra("PATIENT_UID",Uid);
                startActivity(newHealingIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent back = new Intent(this,MainActivity.class);
        back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(back);
    }
}
