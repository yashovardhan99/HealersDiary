package com.yashovardhan99.healersdiary.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.yashovardhan99.healersdiary.Adapters.HealingLogAdapter;
import com.yashovardhan99.healersdiary.Objects.Healing;
import com.yashovardhan99.healersdiary.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nullable;

public class HealingLogs extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    CollectionReference logs;
    //arraylist setup
    final ArrayList<Healing> healings = new ArrayList<>();
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healing_logs);

        //toolbar setup
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Healing logs");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //initialize firestore
        db = FirebaseFirestore.getInstance();
        logs = db.collection("users")
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

                    Date date = dc.getDocument().getDate("Date");
                    Healing healing = new Healing();
                    healing.date = df.format(date);
                    healing.Uid = dc.getDocument().getId();

                    switch (dc.getType()){
                        case ADDED:
                            //new healing added
                            healings.add(0,healing);
                            mAdapter.notifyItemInserted(0);
                            break;
                        case REMOVED:
                            //healing removed
                            int pos = healings.indexOf(healing);
                            if(pos<0)
                                break;
                            healings.remove(healing);
                            mAdapter.notifyItemRemoved(pos);
                            break;
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getTitle().toString()){
            case "Delete":
                deleteHealing(item.getGroupId());
                healings.remove(item.getGroupId());
                Snackbar.make(findViewById(R.id.healingLogRecyclerView),"Record Deleted",Snackbar.LENGTH_LONG);
                mAdapter.notifyItemRemoved(item.getGroupId());
                return true;
        }
        return super.onContextItemSelected(item);
    }

    void deleteHealing(int id){
        Healing healing = healings.get(id);
        logs.document(healing.getUid())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                            Log.d("FIRESTORE","Healing Deleted");
                        else
                            Log.d("FIRESTORE","Error : "+task.getException());
                    }
                });
        logs.getParent().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                double due = 0.00, rate = 0.00;
                if(task.isSuccessful()){
                    if(task.getResult().contains("Rate")){
                       rate = task.getResult().getDouble("Rate");
                    }
                    if(task.getResult().contains("Due"))
                        due = task.getResult().getDouble("Due");
                }
                else
                    Log.d("FIRESTORE", String.valueOf(task.getException()));
                logs.getParent().update("Due",due-rate);
            }
        });
    }

}