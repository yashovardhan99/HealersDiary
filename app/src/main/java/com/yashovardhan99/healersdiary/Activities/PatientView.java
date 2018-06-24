package com.yashovardhan99.healersdiary.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yashovardhan99.healersdiary.R;

import java.text.NumberFormat;
import java.util.Map;

public class PatientView extends AppCompatActivity {

    public String Uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_view);
        setSupportActionBar((Toolbar) findViewById(R.id.patientViewToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Uid = getIntent().getStringExtra("PATIENT_UID");
        if(Uid != null){
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference = db.collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("patients")
                    .document(Uid);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()){
                            Log.d("FIRESTORE","Data exists : "+document.getData());

                            Map<String,Object> patient = document.getData();//data extracted
                            if(patient==null){
                                return;
                            }
                            TextView name = findViewById(R.id.patientNameInDetail);
                            if(patient.containsKey("Name"))
                                name.setText(patient.get("Name").toString());
                            TextView disease = findViewById(R.id.patientDiseaseInDetail);
                            if(patient.containsKey("Disease"))
                                disease.setText(patient.get("Disease").toString());
                            TextView due = findViewById(R.id.bill);
                            if(patient.containsKey("Due")) {
                                Double amt = Double.parseDouble(patient.get("Due").toString());
                                String famt = "Amount Due : "+NumberFormat.getCurrencyInstance().format(amt);
                                due.setText(famt);
                            }
                        }
                    }
                }
            });
        }

    }
}
