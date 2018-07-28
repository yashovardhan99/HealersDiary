package com.yashovardhan99.healersdiary.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.yashovardhan99.healersdiary.Adapters.PatientPaymentLogsAdapter;
import com.yashovardhan99.healersdiary.Objects.PaymentSnapshot;
import com.yashovardhan99.healersdiary.R;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

public class PatientPaymentLogs extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    private final ArrayList<PaymentSnapshot> payments = new ArrayList<>();
    private CollectionReference logs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_payment_logs);

        //toolbar setup
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.payment_logs);

        //fetching collection of payments
        logs = FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("patients")
                .document(getIntent().getStringExtra(MainActivity.PATIENT_UID))
                .collection("payments");

        //fetching payment records here
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
                            if(pos<0)
                                break;
                            payments.remove(payment);
                            mAdapter.notifyItemRemoved(pos);
                            break;
                    }
                }
            }
        });

        RecyclerView mRecyclerView = findViewById(R.id.patientPaymentLogsRecycler);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PatientPaymentLogsAdapter(payments);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final String EDIT = getString(R.string.edit);
        final String DELETE = getString(R.string.delete);
        if(item.getTitle().toString().equals(EDIT)) {
            //edit payment
            Snackbar.make(findViewById(R.id.patientPaymentLogsRecycler), R.string.not_yet_implemented, Snackbar.LENGTH_LONG).show();
            Log.d("CONTEXT MENU", "EDIT - " + item.getGroupId());
            return true;
        }
        else if(item.getTitle().toString().equals(DELETE)) {
            //delete record
            Log.d("CONTEXT MENU", "DELETE - " + item.getGroupId());
            deletePayment(item.getGroupId());
            return true;
        }
        return super.onContextItemSelected(item);
    }
    private void deletePayment(int id){
        //delete the payment record from firestore
        double amount = 0.00;
        try {
            amount = NumberFormat.getCurrencyInstance().parse(payments.get(id).getAmount()).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final double finalAmount = amount;
        logs.getParent()
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            Map<String,Object> data = task.getResult().getData();
                            double due = 0.00;
                            if(task.getResult().contains("Due"))
                                due = task.getResult().getDouble("Due");
                            due = due + finalAmount;
                            logs.getParent().update("Due",due);
                        }
                    }
                });
        logs.document(payments.get(id).getUid()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(MainActivity.FIRESTORE,"Deleted Payment successfully");
                        }
                        else
                        {
                            Log.d(MainActivity.FIRESTORE,"Error - "+task.getException());
                        }
                    }
                });
        Snackbar.make(findViewById(R.id.patientPaymentLogsRecycler),"Payment Deleted : "+NumberFormat.getCurrencyInstance().format(finalAmount),Snackbar.LENGTH_LONG).show();
        payments.remove(id);
        mAdapter.notifyItemRemoved(id);
    }
}
