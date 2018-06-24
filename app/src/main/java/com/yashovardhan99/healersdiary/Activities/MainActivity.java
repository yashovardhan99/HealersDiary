package com.yashovardhan99.healersdiary.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.yashovardhan99.healersdiary.Adapters.MainListAdapter;
import com.yashovardhan99.healersdiary.Objects.Patient;
import com.yashovardhan99.healersdiary.R;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    private ArrayList<Patient> patientList;
    Toolbar mainActivityToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        patientList = new ArrayList<>();

        mainActivityToolbar = findViewById(R.id.mainActivityToolbar);
        setSupportActionBar(mainActivityToolbar);

        //check login and handle
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        if(mUser==null) {//not signed in
            startActivity(new Intent(this, Login.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            return;
        }

        //firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference patients = db.collection("users")
                .document(mUser.getUid())
                .collection("patients");
        //to instantly make any changes reflect here
        patients.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.d("FIRESTORE","ERROR : "+e.getMessage());
                    return;
                }
                Log.d("FIRESTORE","Data fetced");
                for(DocumentChange dc:queryDocumentSnapshots.getDocumentChanges()){
                    //getting changes in documents
                    Log.d("FIRESTORE",dc.getDocument().getData().toString());

                    switch (dc.getType()){

                        case ADDED:
                            //add new patient to arrayList
                            Patient patient = new Patient();
                            patient.name = dc.getDocument().get("Name").toString();
                            patient.uid = dc.getDocument().getId();
                            patientList.add(patient);
                            mAdapter.notifyItemInserted(patientList.indexOf(patient));
                            break;

                        case MODIFIED:
                            //modify patient name
                            String id = dc.getDocument().getId();
                            for(Patient patient1: patientList){
                                if(patient1.getUid().equals(id)){
                                    patient1.name = dc.getDocument().get("Name").toString();
                                    mAdapter.notifyItemChanged(patientList.indexOf(patient1));
                                    break;
                                }
                            }
                            break;
                        case REMOVED:
                            //remove patient record
                            String id2 = dc.getDocument().getId();
                            for(Patient patient1: patientList){
                                if(patient1.getUid().equals(id2)){
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

        //new patient record button
        FloatingActionButton newPatientButton = findViewById(R.id.new_fab);
        newPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,NewPatient.class));
            }
        });
    }
}
