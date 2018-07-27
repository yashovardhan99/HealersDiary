package com.yashovardhan99.healersdiary.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.yashovardhan99.healersdiary.Helpers.HtmlCompat;
import com.yashovardhan99.healersdiary.R;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

public class PatientView extends AppCompatActivity {

    public String Uid;
    public final int REQUEST_HEALING_CONFIRMATION = 0;
    public final int REQUEST_PAYMENT_ADDED = 1;
    public final int REQUEST_FEEDBACK_ADDED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_view);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Patient Detail");
        //toolbar setup

        Uid = getIntent().getStringExtra(MainActivity.PATIENT_UID);
        //patient record UID to fetch records from firestore
        if (Uid != null) {

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            //initialize firestore
            final DocumentReference documentReference = db.collection(MainActivity.USERS)
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                    .collection("patients")
                    .document(Uid);
            //create ref to patient document
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                //listener to update patient data
                @Override
                public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException e) {
                    if (document!=null && document.exists()) {
                        //get document data
                        Log.d(MainActivity.FIRESTORE, "Data exists : " + document.getData());
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
                            String famt = getString(R.string.payment_due)+": <b><big>"+NumberFormat.getCurrencyInstance().format(amt)+"</big></b>";
                            due.setText(HtmlCompat.fromHtml(famt));
                        }
                        else
                            due.setVisibility(View.GONE);
                    }
                }

            });
        }

        //new healing button - to add new healing
        Button newHealing = findViewById(R.id.newHealingButton);
        newHealing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newHealingIntent = new Intent(PatientView.this,NewHealingRecord.class);
                newHealingIntent.putExtra(MainActivity.PATIENT_UID,Uid);
                startActivityForResult(newHealingIntent, REQUEST_HEALING_CONFIRMATION);
            }
        });

        //go to healing logs
        Button healingLogs = findViewById(R.id.healingLogsButton);
        healingLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logs = new Intent(PatientView.this, HealingLogs.class);
                logs.putExtra("PATIENT_NAME",((TextView)findViewById(R.id.patientNameInDetail)).getText());
                logs.putExtra(MainActivity.PATIENT_UID,Uid);
                startActivity(logs);
            }
        });

        //add new payment
        Button addPayment = findViewById(R.id.enterNewPPayment);
        addPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pay = new Intent(PatientView.this,PatientAddPaymentDialog.class);
                pay.putExtra(MainActivity.PATIENT_UID, Uid);
                startActivityForResult(pay,REQUEST_PAYMENT_ADDED);
            }
        });

        //go to payment logs
        Button paymentLogs = findViewById(R.id.viewPatientPaymentLogs);
        paymentLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logs = new Intent(PatientView.this, PatientPaymentLogs.class);
                logs.putExtra(MainActivity.PATIENT_UID,Uid);
                startActivity(logs);
            }
        });

        //add new patient feedback
        Button newFeedback = findViewById(R.id.newFeedbackButton);
        newFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newFeed = new Intent(PatientView.this, NewPatientFeedback.class);
                newFeed.putExtra(MainActivity.PATIENT_UID, Uid);
                startActivityForResult(newFeed,REQUEST_FEEDBACK_ADDED);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent back = new Intent(this,MainActivity.class);
        back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(back);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_HEALING_CONFIRMATION:
                if(resultCode==NewHealingRecord.NEW_HEALING_ADDED_RESULT)
                    Snackbar.make(findViewById(R.id.patientNameInDetail), R.string.added, Snackbar.LENGTH_SHORT).show();
                break;

            case REQUEST_PAYMENT_ADDED:
                if(resultCode==PatientAddPaymentDialog.PAYMENT_ADDED_RESULT)
                    Snackbar.make(findViewById(R.id.patientNameInDetail),getString(R.string.payment_added ,data.getStringExtra("Amount")),Snackbar.LENGTH_LONG).show();
                break;

            case REQUEST_FEEDBACK_ADDED:
                if(resultCode==RESULT_OK)
                    Snackbar.make(findViewById(R.id.patientNameInDetail),R.string.added, Snackbar.LENGTH_SHORT).show();
                break;


            default: super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
