package com.yashovardhan99.healersdiary.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.yashovardhan99.healersdiary.Adapters.HealingLogAdapter;
import com.yashovardhan99.healersdiary.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nullable;

public class HealingLogs extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healing_logs);

        //toolbar setup
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.healingLogsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //arraylist setup
        final ArrayList<String> healings = new ArrayList<>();
        //initialize firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference logs = db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("patients")
                .document(getIntent().getStringExtra("PATIENT_UID"))
                .collection("healings");
        //collection reference to all healing logs for this patient
        logs.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                DateFormat df = DateFormat.getDateTimeInstance();
                if(e!=null){
                    Log.d("FIRESTORE",e.getLocalizedMessage());
                    return;
                }
                Log.d("FIRESTORE","Logs fetched");

                for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                    switch (dc.getType()){

                        case ADDED:
                            //new healing added
                            Date date = dc.getDocument().getDate("Date");
                            healings.add(0,df.format(date));
                            mAdapter.notifyItemInserted(0);
                            break;
                        case REMOVED:
                            //healing removed
                            Date date1 = dc.getDocument().getDate("Date");
                            int pos = healings.indexOf(df.format(date1));
                            healings.remove(df.format(date1));
                            mAdapter.notifyItemRemoved(pos);
                    }
                }
            }
        });

        //recycler view setup
        RecyclerView mRecyclerView = findViewById(R.id.healingLogRecyclerView);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new HealingLogAdapter(healings);
        mRecyclerView.setAdapter(mAdapter);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
                //this is to prevent empty activity from loading up on up button press which could lead to app crash
            default: return super.onOptionsItemSelected(item);
        }
    }
}
