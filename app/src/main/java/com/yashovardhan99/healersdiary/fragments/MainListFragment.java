package com.yashovardhan99.healersdiary.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yashovardhan99.healersdiary.R;
import com.yashovardhan99.healersdiary.activities.MainActivity;
import com.yashovardhan99.healersdiary.activities.NewPatient;

import static com.yashovardhan99.healersdiary.activities.MainActivity.healingsToday;
import static com.yashovardhan99.healersdiary.activities.MainActivity.healingsYesterday;

/**
 * Created by Yashovardhan99 on 05-08-2018 as a part of HealersDiary.
 */
public class MainListFragment extends Fragment {

    private RecyclerView.Adapter mAdapter;
    TextView today;
    TextView yesterday;
    private final String TODAY = "TODAY";
    private final String YEST = "YESTERDAY";

    public MainListFragment(){
        //required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_main_list,container,false);

        Toolbar toolbar = RootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }

        //
        //Recycler view setup
        mAdapter = ((MainActivity) getActivity()).getAdapter();
        RecyclerView mRecyclerView;
        mRecyclerView = RootView.findViewById(R.id.recycler_main);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        //displays the divider line bw each item
        DividerItemDecoration itemLine = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemLine);

        ////new patient record button
        FloatingActionButton newPatientButton = RootView.findViewById(R.id.new_fab);
        newPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPatinetIntent = new Intent(getActivity(), NewPatient.class);
                startActivity(newPatinetIntent);
                //log firebase analytics event
                Bundle newPatient = new Bundle();
                newPatient.putString(FirebaseAnalytics.Param.LOCATION, MainActivity.class.getName());
                newPatient.putString(FirebaseAnalytics.Param.CONTENT_TYPE, MainActivity.NEW);
                newPatient.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,MainActivity.PATIENT_RECORD);
                MainActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, newPatient);
            }
        });


        today = RootView.findViewById(R.id.healingsToday);
        yesterday = RootView.findViewById(R.id.healingsYesterday);

        return RootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTextFields();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void updateTextFields(){
        Log.d("UPDATE","UDT called with : "+healingsToday+" and "+healingsYesterday);
        Resources res = getResources();
        today.setText(res.getQuantityString(R.plurals.healing, healingsToday, healingsToday, getString(R.string.today)));
        yesterday.setText(res.getQuantityString(R.plurals.healing, healingsYesterday, healingsYesterday, getString(R.string.yesterday)));
    }
}
