package com.yashovardhan99.healersdiary.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yashovardhan99.healersdiary.R;
import com.yashovardhan99.healersdiary.activities.MainActivity;
import com.yashovardhan99.healersdiary.activities.PatientView;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PatientAddPaymentDialog extends DialogFragment implements View.OnClickListener, DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {

    TextView dateText,timeText;
    Calendar calendar;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View RootView = inflater.inflate(R.layout.fragment_add_payment_dialog,container,false);

        dateText = RootView.findViewById(R.id.paymentDate);
        timeText = RootView.findViewById(R.id.paymentTime);
        calendar = Calendar.getInstance();
        dateText.setText(DateFormat.getDateInstance().format(calendar.getTime()));
        timeText.setText(DateFormat.getTimeInstance().format(calendar.getTime()));
        dateText.setOnClickListener(this);
        timeText.setOnClickListener(this);

        //firestore init
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference patient = db.collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("patients")
                .document(getActivity().getIntent().getStringExtra(MainActivity.PATIENT_UID));

        Button save = RootView.findViewById(R.id.saveNewPayment);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save new healing
                TextInputEditText amt = RootView.findViewById(R.id.paymentReceived);
                final double amount;
                try{
                    amount = Double.parseDouble(amt.getText().toString());
                }catch (Exception e){
                    amt.setError(getString(R.string.enter_valid_amt));
                    return;
                }
                patient.get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                double due = 0.00;
                                if(task.isSuccessful()){
                                    if(task.getResult().getData().containsKey("Due"))
                                        due = Double.parseDouble(task.getResult().getData().get("Due").toString());
                                    due = due - amount;
                                    patient.update("Due",due);
                                }
                            }
                        });
                Map<String,Object> payment = new HashMap<>();
                payment.put("Date",calendar.getTime());
                payment.put("Amount",amount);
                patient.collection("payments")
                        .document(String.valueOf(Calendar.getInstance().getTimeInMillis()))
                        .set(payment)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Log.d("FIRESTORE","Payment record added");
                                else {
                                    Log.d("FIRESTORE", "Payment record added error : " + task.getException().getMessage());
                                }
                            }
                        });
                //amount added
                ((PatientView)(getActivity())).paymentAdded(NumberFormat.getCurrencyInstance().format(amount));
                dismiss();
            }
        });
        return RootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.paymentDate:
                DialogFragment datePickerFragment = new DatePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putLong("DATE",calendar.getTimeInMillis());
                datePickerFragment.setArguments(bundle);
                datePickerFragment.setTargetFragment(this,0);
                datePickerFragment.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "datePicker");
                break;
            case R.id.paymentTime:
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
