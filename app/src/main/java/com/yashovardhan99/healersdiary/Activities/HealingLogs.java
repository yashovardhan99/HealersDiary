package com.yashovardhan99.healersdiary.Activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

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
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import javax.annotation.Nullable;

public class HealingLogs extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    CollectionReference logs;
    //arraylist setup
    final ArrayList<Healing> healings = new ArrayList<>();
    FirebaseFirestore db;
    public int this_day, this_month, last_month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healing_logs);

        //toolbar setup
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getIntent().getStringExtra("PATIENT_NAME"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setting counters
        this_day = 0;
        this_month = 0;
        last_month = 0;

        //initialize firestore
        db = FirebaseFirestore.getInstance();
        logs = db.collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection("patients")
                .document(getIntent().getStringExtra(MainActivity.PATIENT_UID))
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

                //fetching changes to this patient's healing collection
                if (queryDocumentSnapshots != null) {
                    for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){

                        Date date = dc.getDocument().getDate("Date");
                        Healing healing = new Healing();
                        healing.date = df.format(date);
                        healing.Uid = dc.getDocument().getId();

                        //get today's date
                        Calendar day =Calendar.getInstance();
                        day.setTime(new Date());
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);

                        Boolean today = DateUtils.isToday(cal.getTimeInMillis());//check today
                        Boolean month = cal.get(Calendar.MONTH) == day.get(Calendar.MONTH);//check this month
                        //for last month
                        cal.add(Calendar.MONTH, 1);
                        Boolean last = cal.get(Calendar.MONTH) == day.get(Calendar.MONTH);

                        switch (dc.getType()){
                            case ADDED:
                                //new healing added
                                healings.add(0,healing);
                                mAdapter.notifyItemInserted(0);
                                //increment counters appropriately
                                if(today)
                                    this_day++;
                                if(month)
                                    this_month++;
                                if(last)
                                    last_month++;
                                break;
                            case REMOVED:
                                //healing removed
                                //decrement counters
                                if(today)
                                    this_day--;
                                if(month)
                                    this_month--;
                                if(last)
                                    last_month--;
                                int pos = healings.indexOf(healing);
                                if(pos<0)
                                    break;
                                healings.remove(healing);
                                mAdapter.notifyItemRemoved(pos);
                                break;
                        }
                        //NOTE - healing data cannot be modified
                        updateTextFields();
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

        //for the divider lines between each record
        DividerItemDecoration itemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

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
                //record to be deleted
                deleteHealing(item.getGroupId());//deleted from firestore
                healings.remove(item.getGroupId());//removed from list
                Snackbar.make(findViewById(R.id.healingLogRecyclerView),"Record Deleted",Snackbar.LENGTH_LONG);
                mAdapter.notifyItemRemoved(item.getGroupId());//updated adapter
                return true;
        }
        return super.onContextItemSelected(item);
    }

    void deleteHealing(int id){
        //deletes healing from firestore
        Healing healing = healings.get(id);
        logs.document(healing.getUid())
                .delete();
        //now changing amount due
        Objects.requireNonNull(logs.getParent()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                double due = 0.00, rate = 0.00;
                if(task.isSuccessful() && task.getResult()!=null){
                    if(task.getResult().contains("Rate")){
                       rate = Objects.requireNonNull(task.getResult().getDouble("Rate"));
                    }
                    if(task.getResult().contains("Due"))
                        due = Objects.requireNonNull(task.getResult().getDouble("Due"));
                }
                else
                    Log.d("FIRESTORE", String.valueOf(task.getException()));

                logs.getParent().update("Due",due-rate);//updating amount due
            }
        });
    }
    void updateTextFields(){
        TextView today = findViewById(R.id.todayHealings);
        TextView month = findViewById(R.id.thisMonthHealings);
        TextView last = findViewById(R.id.lastMonthHealings);

        Resources res = getResources();

        today.setText(res.getQuantityString(R.plurals.healing,this_day,this_day,getString(R.string.today)));
        month.setText(res.getQuantityString(R.plurals.healing,this_month,this_month,getString(R.string.this_month)));
        last.setText(res.getQuantityString(R.plurals.healing, last_month, last_month, getString(R.string.last_month)));
    }
}