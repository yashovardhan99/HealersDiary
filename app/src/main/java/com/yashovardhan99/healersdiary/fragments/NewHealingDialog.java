package com.yashovardhan99.healersdiary.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yashovardhan99.healersdiary.R;
import com.yashovardhan99.healersdiary.activities.MainActivity;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Yashovardhan99 on 07-08-2018 as a part of HealersDiary.
 */
public class NewHealingDialog extends DialogFragment implements View.OnClickListener, DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {

    TextView dateText,timeText;
    Calendar calendar;

    public NewHealingDialog(){
        //mandatory empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        View RootView = inflater.inflate(R.layout.fragment_new_healing_dialog,container,false);
        dateText = RootView.findViewById(R.id.healingDate);
        timeText = RootView.findViewById(R.id.healingTime);
        calendar = Calendar.getInstance();
        dateText.setText(DateFormat.getDateInstance().format(calendar.getTime()));
        timeText.setText(DateFormat.getTimeInstance().format(calendar.getTime()));
        dateText.setOnClickListener(this);
        timeText.setOnClickListener(this);

        RootView.findViewById(R.id.saveNewHealing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(R.id.patientNameInDetail),R.string.added,Snackbar.LENGTH_SHORT).show();
                dismiss();
            }
        });
        return RootView;
    }

    void saveData(){
        String Uid = Objects.requireNonNull(getActivity()).getIntent().getStringExtra(MainActivity.PATIENT_UID);
        Log.d("PATIENT UID RECEIVED",Uid);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference patient = db.collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
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
                            Map<String, Object> data = Objects.requireNonNull(task.getResult()).getData();
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
                                                Log.d("FIRESTORE",Objects.requireNonNull(task.getException()).getMessage());
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
        healingData.put("Date",calendar.getTime());
        healing.set(healingData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            Log.d("FIRESTORE","New Healing data added");
                        else
                            Log.d("FIRESTORE","Error while adding new healing data : "+Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.healingDate:
                DialogFragment datePickerFragment = new DatePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putLong("DATE",calendar.getTimeInMillis());
                datePickerFragment.setArguments(bundle);
                datePickerFragment.setTargetFragment(this,0);
                datePickerFragment.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "datePicker");
                break;
            case R.id.healingTime:
                DialogFragment timePickerFragment = new TimePickerFragment();
                Bundle time = new Bundle();
                time.putLong("DATE",calendar.getTimeInMillis());
                timePickerFragment.setArguments(time);
                timePickerFragment.setTargetFragment(this,0);
                timePickerFragment.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "timePicker");
                break;
        }
    }

    @Override
    public void onDateSet(DialogFragment dialogFragment) {
        DatePickerFragment dpf = (DatePickerFragment) dialogFragment;
        calendar.set(dpf.getYear(), dpf.getMonth(), dpf.getDay());
        dateText.setText(DateFormat.getDateInstance().format(calendar.getTime()));
        timeText.performClick();
    }

    @Override
    public void onTimeSet(DialogFragment dialogFragment) {
        TimePickerFragment tpf = (TimePickerFragment) dialogFragment;
        calendar.set(Calendar.HOUR_OF_DAY, tpf.hour);
        calendar.set(Calendar.MINUTE, tpf.minute);
        calendar.set(Calendar.SECOND, 0);
        timeText.setText(DateFormat.getTimeInstance().format(calendar.getTime()));
    }
}
