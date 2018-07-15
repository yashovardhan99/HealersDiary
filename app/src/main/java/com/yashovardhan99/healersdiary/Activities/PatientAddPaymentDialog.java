package com.yashovardhan99.healersdiary.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yashovardhan99.healersdiary.R;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PatientAddPaymentDialog extends AppCompatActivity {

    public static final int PAYMENT_ADDED_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_add_payment_dialog);

        //firestore init
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference patient = db.collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("patients")
                .document(getIntent().getStringExtra(MainActivity.PATIENT_UID));

        Button save = findViewById(R.id.saveNewPayment);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save new healing
                TextInputEditText amt = findViewById(R.id.paymentReceived);
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
                payment.put("Date",Calendar.getInstance().getTime());
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
                //to return the amount of payment added
                Intent data = new Intent();
                data.putExtra("Amount", NumberFormat.getCurrencyInstance().format(amount));
                setResult(PAYMENT_ADDED_RESULT,data);
                finish();
            }
        });
    }
}
