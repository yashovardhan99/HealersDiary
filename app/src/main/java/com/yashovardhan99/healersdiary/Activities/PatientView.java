package com.yashovardhan99.healersdiary.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.yashovardhan99.healersdiary.R;

public class PatientView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_view);
        setSupportActionBar((Toolbar) findViewById(R.id.patientViewToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
