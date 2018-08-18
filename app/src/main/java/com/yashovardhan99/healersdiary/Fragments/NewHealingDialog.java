package com.yashovardhan99.healersdiary.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yashovardhan99.healersdiary.Activities.MainActivity;
import com.yashovardhan99.healersdiary.Activities.PatientView;
import com.yashovardhan99.healersdiary.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yashovardhan99 on 07-08-2018 as a part of HealersDiary.
 */
public class NewHealingDialog extends DialogFragment {

    public NewHealingDialog(){
        //mandatory empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        View RootView = inflater.inflate(R.layout.activity_new_healing_record,container,false);
        RootView.findViewById(R.id.saveNewHealing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                Snackbar.make(((PatientView)getActivity()).findViewById(R.id.patientNameInDetail),R.string.added,Snackbar.LENGTH_SHORT).show();
                dismiss();
            }
        });
        return RootView;
    }

    void saveData(){
        String Uid = ((PatientView)getActivity()).getIntent().getStringExtra(MainActivity.PATIENT_UID);
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
                            if (data == null) {
                                Log.d("FIRESTORE","Error Data is null");
                                return;
                            }

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
                                                Toast.makeText(getContext(), R.string.something_went_wrong_adding_record,Toast.LENGTH_LONG).show();
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
