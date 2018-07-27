package com.yashovardhan99.healersdiary.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yashovardhan99.healersdiary.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewHealingRecord extends AppCompatActivity {

    public static final int NEW_HEALING_ADDED_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_healing_record);

        findViewById(R.id.saveNewHealing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveData();
                setResult(NEW_HEALING_ADDED_RESULT);
                finish();
            }
        });
    }
    private void SaveData(){
        //saves data to firestore
        String Uid = getIntent().getStringExtra(MainActivity.PATIENT_UID);
        Log.d("PATIENT UID RECEIVED",Uid);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference patient = db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("patients")
                .document(Uid);
        //get patient reference
        patient.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        double rate = 0.00;
                        double due = 0.00;
                        if(task.isSuccessful()){
                            Log.d("FIRESTORE","Data fetched");
                            Map<String, Object> data = task.getResult().getData();
                            if(data.containsKey("Rate") && data.get("Rate") != null)
                                rate = Double.parseDouble(data.get("Rate").toString());
                            if(data.containsKey("Due") && data.get("Due") != null)
                                due = Double.parseDouble(data.get("Due").toString());
                            due = due + rate;
                            patient.update("Due",due)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(!task.isSuccessful()){
                                                Toast.makeText(NewHealingRecord.this, R.string.something_went_wrong_adding_record,Toast.LENGTH_LONG).show();
                                                Log.d("FIRESTORE",task.getException().getMessage());
                                            }
                                            else
                                                Log.d("FIRESTORE","Data updated");
                                        }
                                    });
                        }

                    }
                });
        DocumentReference healing = patient.collection("healings")
                .document(Long.toString(Calendar.getInstance().getTimeInMillis()));
        Map<String,Object> healingData = new HashMap<>();
        healingData.put("Date",Calendar.getInstance().getTime());
        healing.set(healingData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            Log.d("FIRESTORE","New Healing data added");
                        else
                            Log.d("FIRESTORE","Error while adding new healing data : "+task.getException().getMessage());
                    }
                });
    }
}
