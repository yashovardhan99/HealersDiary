package com.yashovardhan99.healersdiary.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.yashovardhan99.healersdiary.R;
import com.yashovardhan99.healersdiary.adapters.PatientFeedbackListAdapter;
import com.yashovardhan99.healersdiary.objects.PatientFeedback;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class PatientFeedbackLogs extends AppCompatActivity {

    private RecyclerView.Adapter mAdapter;
    private final ArrayList<PatientFeedback> feedbacks = new ArrayList<>();
    private CollectionReference logs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_feedback_logs);

        //toolbar setup
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.patient_feedbacks);

        //fetching collection of feedbacks
        logs = FirebaseFirestore.getInstance()
                .collection(MainActivity.USERS)
                .document(FirebaseAuth.getInstance().getUid())
                .collection("patients")
                .document(getIntent().getStringExtra(MainActivity.PATIENT_UID))
                .collection("feedbacks");

        //fetching records
        logs.orderBy("Date", Query.Direction.DESCENDING).limit(100)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            PatientFeedback feedback = new PatientFeedback(dc.getDocument().getId(),
                                    dc.getDocument().getString("Feedback"),
                                    dc.getDocument().getTimestamp("Date"),
                                    dc.getDocument().contains("Verified"));

                            switch (dc.getType()) {

                                case ADDED:
                                    feedbacks.add(feedback);
                                    mAdapter.notifyItemInserted(feedbacks.size() - 1);
                                    break;

                                case MODIFIED:
                                    for (PatientFeedback patientFeedback : feedbacks) {
                                        if (patientFeedback.getUid().equals(feedback.getUid())) {
                                            int index = feedbacks.indexOf(patientFeedback);
                                            feedbacks.set(index, feedback);
                                            mAdapter.notifyItemChanged(index);
                                            break;
                                        }
                                    }

                                case REMOVED:
                                    int pos = feedbacks.indexOf(feedback);
                                    if (pos < 0)
                                        break;
                                    feedbacks.remove(feedback);
                                    mAdapter.notifyItemRemoved(pos);
                            }
                        }
                    }
                });

        //assign recycler view and adapters
        RecyclerView mRecyclerView = findViewById(R.id.PatientFeedbackLogsRecycler);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PatientFeedbackListAdapter(feedbacks);
        mRecyclerView.setAdapter(mAdapter);

        //adds a line decor after each item
        DividerItemDecoration itemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    //to handle up button press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
    //Edit or delete records
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final String EDIT = getString(R.string.edit);
        final String DELETE = getString(R.string.delete);
        if(item.getTitle().toString().equals(EDIT)) {
            //edit payment
            Snackbar.make(findViewById(R.id.PatientFeedbackLogsRecycler), R.string.not_yet_implemented, Snackbar.LENGTH_LONG).show();
            Log.d("CONTEXT MENU", "EDIT - " + item.getGroupId());
            return true;
        }
        else if(item.getTitle().toString().equals(DELETE)) {
            //delete record
            Log.d("CONTEXT MENU", "DELETE - " + item.getGroupId());
            deleteFeedback(item.getGroupId());
            return true;
        }
        return super.onContextItemSelected(item);
    }

    void deleteFeedback(int id){
        logs.document(feedbacks.get(id).getUid()).delete();
        Snackbar.make(findViewById(R.id.PatientFeedbackLogsRecycler), R.string.deleted,Snackbar.LENGTH_SHORT).show();
        feedbacks.remove(id);
        mAdapter.notifyItemRemoved(id);
    }
}
