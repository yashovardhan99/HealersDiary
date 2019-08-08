package com.yashovardhan99.healersdiary.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.yashovardhan99.healersdiary.R;
import com.yashovardhan99.healersdiary.activities.MainActivity;
import com.yashovardhan99.healersdiary.objects.Patient;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class PatientBillView extends DialogFragment implements com.google.firebase.firestore.EventListener<DocumentSnapshot>, View.OnClickListener {

    int billtype;
    Boolean detailed;
    String uid;
    Patient patient;
    TextView billText;
    DocumentReference documentReference;
    ListenerRegistration registration;
    com.google.firebase.firestore.EventListener<QuerySnapshot> healingListener = new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            int no = 0;
            assert queryDocumentSnapshots != null;
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                no++;
                if (detailed) {
                    String date = DateFormat.getDateTimeInstance().format(documentSnapshot.getDate("Date"));
                    billText.setText(billText.getText() + "\n" + date);
                }
            }
            patient.setDue(patient.getRate() * no);
            billText.setText(billText.getText() + "\n" + getString(R.string.bill,
                    patient.getName(),
                    DateFormat.getDateInstance().format(Calendar.getInstance().getTime()),
                    NumberFormat.getCurrencyInstance().format(patient.getDue()),
                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
        }
    };


    public PatientBillView() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View RootView = inflater.inflate(R.layout.fragment_patient_bill_view, container, false);
        billText = RootView.findViewById(R.id.billText);
        Button share = RootView.findViewById(R.id.shareBill);
        Button close = RootView.findViewById(R.id.closeDialog);
        close.setOnClickListener(this);
        share.setOnClickListener(this);
        Bundle bundle = getArguments();
        uid = bundle.getString(MainActivity.PATIENT_UID);
        billtype = bundle.getInt(MainActivity.BILL_TYPE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        documentReference = db.collection(MainActivity.USERS)
                .document(FirebaseAuth.getInstance().getUid())
                .collection("patients")
                .document(uid);
        registration = documentReference.addSnapshotListener(this);
        patient = new Patient();
        patient.setUid(uid);
        detailed = bundle.getBoolean(MainActivity.INCLUDE_LOGS);
        return RootView;
    }

    @Override
    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
        assert documentSnapshot != null;
        patient.setName(documentSnapshot.getString("Name"));
        patient.setDue(Double.parseDouble(Objects.requireNonNull(documentSnapshot.get("Due")).toString()));
        patient.setRate(Double.parseDouble(Objects.requireNonNull(documentSnapshot.get("Rate")).toString()));
        Calendar thisMonth = Calendar.getInstance();
        thisMonth.set(Calendar.HOUR_OF_DAY, 0);
        thisMonth.clear(Calendar.MINUTE);
        thisMonth.clear(Calendar.SECOND);
        thisMonth.clear(Calendar.MILLISECOND);
        thisMonth.set(Calendar.DAY_OF_MONTH, 1);
        switch (billtype) {
            case R.id.allBillButton:
                billText.setText(getString(R.string.bill, patient.getName(),
                        DateFormat.getDateInstance().format(Calendar.getInstance().getTime()),
                        NumberFormat.getCurrencyInstance().format(patient.getDue()),
                        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName()));
                break;
            case R.id.thisMonthBillButton:
                registration.remove();
                registration = documentReference.collection("healings").orderBy("Date", Query.Direction.ASCENDING)
                        .whereGreaterThanOrEqualTo("Date", new Timestamp(thisMonth.getTime()))
                        .addSnapshotListener(healingListener);
                break;
            case R.id.lastMonthBillButton:
                Calendar lastMonth = Calendar.getInstance();
                lastMonth.add(Calendar.MONTH,-1);
                lastMonth.set(Calendar.DATE,1);
                lastMonth.set(Calendar.HOUR_OF_DAY,0);
                lastMonth.set(Calendar.MINUTE,0);
                lastMonth.set(Calendar.SECOND,0);
                lastMonth.set(Calendar.MILLISECOND,0);
                Log.d("THISMONTH",thisMonth.getTime().toString());
                Log.d("LASTMONTH",lastMonth.getTime().toString());
                registration.remove();
                registration = documentReference.collection("healings")
                        .whereGreaterThanOrEqualTo("Date", new Timestamp(lastMonth.getTime()))
                        .whereLessThan("Date", new Timestamp(thisMonth.getTime()))
                        .orderBy("Date", Query.Direction.ASCENDING)
                        .addSnapshotListener(healingListener);
                break;
        }
    }

    @Override
    public void onDetach() {
        registration.remove();
        healingListener = null;
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.shareBill:
                billText.setSelected(true);
                String text = billText.getText().toString();
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_TEXT,text);
                share.setType("text/plain");
                startActivity(Intent.createChooser(share,"Send Bill"));
                break;
            case R.id.closeDialog:
                dismiss();
                break;
        }
    }
}
