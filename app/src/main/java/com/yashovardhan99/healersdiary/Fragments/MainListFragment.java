package com.yashovardhan99.healersdiary.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.yashovardhan99.healersdiary.Activities.MainActivity;
import com.yashovardhan99.healersdiary.Adapters.MainListAdapter;
import com.yashovardhan99.healersdiary.Objects.Patient;
import com.yashovardhan99.healersdiary.R;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Yashovardhan99 on 05-08-2018 as a part of HealersDiary.
 */
public class MainListFragment extends Fragment {

    private RecyclerView.Adapter mAdapter;
    public ArrayList<Patient> patientList;
    public FirebaseFirestore db;
    private CollectionReference patients;
    EventListener<QuerySnapshot> queryDocumentSnapshots;


    public MainListFragment(){
        //required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_main_list,container,false);
        patientList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        patients = db.collection(MainActivity.USERS)
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("patients");

        //to instantly make any changes reflect here
        queryDocumentSnapshots = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(MainActivity.FIRESTORE, "ERROR : " + e.getMessage());
                    return;
                }
                Log.d(MainActivity.FIRESTORE, "Data fetced");
                if(queryDocumentSnapshots==null || getActivity()==null)
                    return;
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    //getting changes in documents
                    Log.d(MainActivity.FIRESTORE, dc.getDocument().getData().toString());

                    switch (dc.getType()) {

                        case ADDED:
                            //add new patient to arrayList
                            Patient patient = new Patient();
                            patient.name = Objects.requireNonNull(dc.getDocument().get("Name")).toString();
                            patient.uid = dc.getDocument().getId();
                            patientList.add(patient);
                            mAdapter.notifyItemInserted(patientList.indexOf(patient));
                            ((MainActivity) getActivity()).countHealings(patient.getUid());
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
        };

        patients.addSnapshotListener(queryDocumentSnapshots);

        //Recycler view setup
        RecyclerView mRecyclerView;
        mRecyclerView = RootView.findViewById(R.id.recycler_main);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new MainListAdapter(patientList);
        mRecyclerView.setAdapter(mAdapter);

        //displays the divider line bw each item
        DividerItemDecoration itemLine = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemLine);

        return RootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
