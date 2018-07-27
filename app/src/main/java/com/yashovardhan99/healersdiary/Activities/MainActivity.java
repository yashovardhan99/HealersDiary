package com.yashovardhan99.healersdiary.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.yashovardhan99.healersdiary.Adapters.MainListAdapter;
import com.yashovardhan99.healersdiary.Objects.Patient;
import com.yashovardhan99.healersdiary.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    public static final String PATIENT_UID = "PATIENT_UID";
    public static final String PATIENT_RECORD = "patient_record";
    public static final String DELETE_BUTTON = "Delete Button";
    public static final String EDIT_BUTTON = "Edit Button";
    public static final String NEW = "New";
    public static final String USERS = "users";
    public static final String FIRESTORE = "FIRESTORE";
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Patient> patientList;
    Toolbar mainActivityToolbar;
    private FirebaseAnalytics mFirebaseAnalytics;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    public static int healingsToday, healingsYesterday;
    public static BillingClient mBillingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        patientList = new ArrayList<>();

        mainActivityToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mainActivityToolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setTitle(R.string.app_name);

        //check login and handle
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            //not signed in
            startActivity(new Intent(this, Login.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return;
        }


        //initialize healings counter
        healingsToday = 0;
        healingsYesterday = 0;

        //display welcome message
        if (mUser.getDisplayName() != null)
            ((TextView) findViewById(R.id.userWelcome)).setText(getString(R.string.welcome_user, mUser.getDisplayName()));
        else
            findViewById(R.id.userWelcome).setVisibility(View.GONE);

        //firestore init
        db = FirebaseFirestore.getInstance();
        CollectionReference patients = db.collection(USERS)
                .document(mUser.getUid())
                .collection("patients");

        //to instantly make any changes reflect here
        patients.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(FIRESTORE, "ERROR : " + e.getMessage());
                    return;
                }
                Log.d(FIRESTORE, "Data fetced");
                if(queryDocumentSnapshots==null)
                    return;
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    //getting changes in documents
                    Log.d(FIRESTORE, dc.getDocument().getData().toString());

                    switch (dc.getType()) {

                        case ADDED:
                            //add new patient to arrayList
                            Patient patient = new Patient();
                            patient.name = Objects.requireNonNull(dc.getDocument().get("Name")).toString();
                            patient.uid = dc.getDocument().getId();
                            patientList.add(patient);
                            mAdapter.notifyItemInserted(patientList.indexOf(patient));
                            countHealings(patient.getUid());
                            break;

                        case MODIFIED:
                            //modify patient name
                            String id = dc.getDocument().getId();
                            for (Patient patient1 : patientList) {
                                if (patient1.getUid().equals(id)) {
                                    patient1.name = Objects.requireNonNull(dc.getDocument().get("Name")).toString();
                                    mAdapter.notifyItemChanged(patientList.indexOf(patient1));
                                    break;
                                }
                            }
                            break;
                        case REMOVED:
                            //remove patient record
                            String id2 = dc.getDocument().getId();
                            for (Patient patient1 : patientList) {
                                if (patient1.getUid().equals(id2)) {
                                    int pos = patientList.indexOf(patient1);
                                    patientList.remove(patient1);
                                    mAdapter.notifyItemRemoved(pos);
                                    break;
                                }
                            }
                            break;
                    }
                }
            }
        });

        //Recycler view setup
        RecyclerView mRecyclerView;
        mRecyclerView = findViewById(R.id.recycler_main);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MainListAdapter(patientList);
        mRecyclerView.setAdapter(mAdapter);

        //displays the divider line bw each item
        DividerItemDecoration itemLine = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemLine);

        //new patient record button
        FloatingActionButton newPatientButton = findViewById(R.id.new_fab);
        newPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPatinetIntent = new Intent(MainActivity.this, NewPatient.class);
                startActivity(newPatinetIntent);
                //log firebase analytics event
                Bundle newPatient = new Bundle();
                newPatient.putString(FirebaseAnalytics.Param.LOCATION, MainActivity.class.getName());
                newPatient.putString(FirebaseAnalytics.Param.CONTENT_TYPE, NEW);
                newPatient.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,PATIENT_RECORD);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, newPatient);
            }
        });
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        String title = item.getTitle().toString();
        final String EDIT = getString(R.string.edit);
        final String DELETE = getString(R.string.delete);
        if (title.equals(EDIT)) {
            //edit patient data
            Intent editPatient = new Intent(this, NewPatient.class);
            editPatient.putExtra("EDIT", true);
            String id = patientList.get(item.getGroupId()).getUid();
            editPatient.putExtra(PATIENT_UID, id);
            startActivity(editPatient);

            //log analytics
            Bundle edit = new Bundle();
            edit.putString(FirebaseAnalytics.Param.LOCATION, MainActivity.class.getName());
            edit.putString(FirebaseAnalytics.Param.CONTENT_TYPE, EDIT_BUTTON);
            edit.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, PATIENT_RECORD);
            edit.putString(FirebaseAnalytics.Param.ITEM_ID, id);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, edit);

            return true;
        } else if (title.equals(DELETE)) {
            //delete patient data
            final AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(MainActivity.this);
            confirmBuilder.setMessage(R.string.delete_warning_message)
                    .setTitle(R.string.sure_q)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DeletePatientRecord(item.getGroupId());
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //action cancelled
                        }
                    });
            //to confirm deletion
            AlertDialog confirm = confirmBuilder.create();
            confirm.show();
            return true;
        } else
            return super.onContextItemSelected(item);
    }

    void DeletePatientRecord(int id) {
        DocumentReference patient = db.collection(USERS)
                .document(Objects.requireNonNull(mAuth.getUid()))
                .collection("patients")
                .document(patientList.get(id).getUid());
        //now to delete this record, we first delete all healing and payment history of this patient
        final CollectionReference healings = patient
                .collection("healings");
        healings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        documentSnapshot.getReference().delete();
                    }
                }
            }
        });

        CollectionReference payments = patient.collection("payments");
        payments.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        documentSnapshot.getReference().delete();
                    }
                }
            }
        });

        //now deleting patient document
        patient.delete();
        Snackbar.make(findViewById(R.id.recycler_main), R.string.deleted, Snackbar.LENGTH_LONG).show();

        //logging in analytics
        Bundle delete = new Bundle();
        delete.putString(FirebaseAnalytics.Param.CONTENT_TYPE, DELETE_BUTTON);
        delete.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, PATIENT_RECORD);
        delete.putString(FirebaseAnalytics.Param.LOCATION,MainActivity.class.getName());
        delete.putString(FirebaseAnalytics.Param.ITEM_ID,patient.getId());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, delete);
    }
    void countHealings(String uid) {
        Log.d("COUNTING HEALINGS", uid);

        //yesterday's timestamp
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -2);
        Date yest = yesterday.getTime();
        Timestamp y = new Timestamp(yest);

        db.collection(USERS)
                .document(Objects.requireNonNull(mAuth.getUid()))
                .collection("patients")
                .document(uid)
                .collection("healings")
                .orderBy("Date", Query.Direction.DESCENDING)
                .whereGreaterThan("Date",y)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                         if(queryDocumentSnapshots == null)
                             return;
                        for(DocumentChange dc:queryDocumentSnapshots.getDocumentChanges()){
                            Timestamp timestamp = dc.getDocument().getTimestamp("Date");
                            if(timestamp==null)
                                continue;
                            Log.d("COUNTING HEALINGS",timestamp.toString());
                            Long time = timestamp.toDate().getTime();
                            switch (dc.getType()){
                                case ADDED:
                                    if(DateUtils.isToday(time))
                                        healingsToday++;
                                    else if(DateUtils.isToday(time+DateUtils.DAY_IN_MILLIS))
                                        healingsYesterday++;
                                    break;
                                case REMOVED:
                                    if(DateUtils.isToday(time))
                                        healingsToday--;
                                    else if(DateUtils.isToday(time+DateUtils.DAY_IN_MILLIS))
                                        healingsYesterday--;
                                    break;
                            }
                        }
                        updateTextFields();
                    }
                });
    }
    void updateTextFields(){
        TextView today = findViewById(R.id.healingsToday);
        TextView yesterday = findViewById(R.id.healingsYesterday);
        Resources res = getResources();
        today.setText(res.getQuantityString(R.plurals.healing, healingsToday, healingsToday, getString(R.string.today)));
        yesterday.setText(res.getQuantityString(R.plurals.healing, healingsYesterday, healingsYesterday, getString(R.string.yesterday)));
    }
}