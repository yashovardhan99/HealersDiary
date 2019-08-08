package com.yashovardhan99.healersdiary.fragments;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import com.yashovardhan99.healersdiary.R;
import com.yashovardhan99.healersdiary.activities.MainActivity;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class GeneratePatientBill extends DialogFragment implements View.OnClickListener {

    String uid;
    Switch includeLogs;

    public GeneratePatientBill() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View RootView = inflater.inflate(R.layout.fragment_generate_patient_bill, container, false);
        Button thisMonth = RootView.findViewById(R.id.thisMonthBillButton);
        Button lastMonth = RootView.findViewById(R.id.lastMonthBillButton);
        Button all = RootView.findViewById(R.id.allBillButton);
        includeLogs = RootView.findViewById(R.id.include_healing_logs_switch);
        thisMonth.setOnClickListener(this);
        lastMonth.setOnClickListener(this);
        all.setOnClickListener(this);
        assert getArguments() != null;
        uid = getArguments().getString(MainActivity.PATIENT_UID);
        return RootView;
    }

    @Override
    public void onClick(View v) {
        DialogFragment patientBillView = new PatientBillView();
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.PATIENT_UID, uid);
        bundle.putInt(MainActivity.BILL_TYPE, v.getId());
        bundle.putBoolean(MainActivity.INCLUDE_LOGS,includeLogs.isChecked());
        patientBillView.setArguments(bundle);
        patientBillView.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "billview");
        dismiss();
    }
}
