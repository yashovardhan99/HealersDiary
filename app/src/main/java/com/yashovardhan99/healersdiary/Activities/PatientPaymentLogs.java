package com.yashovardhan99.healersdiary.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.yashovardhan99.healersdiary.Adapters.PatientPaymentLogsAdapter;
import com.yashovardhan99.healersdiary.Objects.PaymentSnapshot;
import com.yashovardhan99.healersdiary.R;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.annotation.Nullable;

public class PatientPaymentLogs extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_payment_logs);

        //toolbar setup
        setSupportActionBar((Toolbar) findViewById(R.id.patientPaymentLogsToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ArrayList<PaymentSnapshot> payments = new ArrayList<>();

        //fetching collection of payments
        CollectionReference logs = FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("patients")
                .document(getIntent().getStringExtra("PATIENT_UID"))
                .collection("payments");
        logs.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                DateFormat df = DateFormat.getDateTimeInstance();
                for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                    PaymentSnapshot payment = new PaymentSnapshot();
                    payment.date = df.format(dc.getDocument().getDate("Date"));
                    payment.Uid = dc.getDocument().getId();
                    payment.amount = NumberFormat.getCurrencyInstance().format(dc.getDocument().getDouble("Amount"));
                    switch (dc.getType()){
                        case ADDED:
                            payments.add(0,payment);
                            mAdapter.notifyItemInserted(0);
                            break;
                        case REMOVED:
                            int pos = payments.indexOf(payment);
                            payments.remove(payment);
                            mAdapter.notifyItemRemoved(pos);
                    }
                }
            }
        });

        RecyclerView mRecyclerView = findViewById(R.id.patientPaymentLogsRecycler);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PatientPaymentLogsAdapter(payments);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
