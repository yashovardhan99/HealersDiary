package com.yashovardhan99.healersdiary.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.yashovardhan99.healersdiary.R;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class NewPatient extends AppCompatActivity {

    final int READ_CONTACT_PERMISSION_REQUEST_CODE = 1;
    final int CONTACT_PICKER_REQUEST_CODE = 2;
    TextInputEditText patientNameEditText;
    TextInputEditText contactNumberEditText;
    TextInputEditText rate;
    TextInputEditText disease;
    TextInputEditText due;
    Toolbar newPatientToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient);

        newPatientToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(newPatientToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.add_new_patient);
        //toolbar setup

         patientNameEditText = findViewById(R.id.patientName);
        contactNumberEditText = findViewById(R.id.phoneNumber);
        rate = findViewById(R.id.rate);
        due = findViewById(R.id.EnterPaymentDue);
        disease = findViewById(R.id.patientDisease);

        //Contact Picker Button
        Button contactBrowse = findViewById(R.id.contact_picker_button);
        contactBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(NewPatient.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                    //permission to read contacts not granted
                    Log.d("CONTACT PERMISSION","NOT GRANTED");
                    ActivityCompat.requestPermissions(NewPatient.this,new String[]{Manifest.permission.READ_CONTACTS},
                            READ_CONTACT_PERMISSION_REQUEST_CODE);//Requesting contact permission
                }
                else
                {
                    //permission is there
                    //call method for choosing contact
                    Log.d("CONTACT PERMISSION","GRANTED ALREADY");
                    launchContactPicker();
                }
            }
        });

        //add the currency symbol to the rate field
        rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty() && !(s.toString().startsWith(NumberFormat.getCurrencyInstance().getCurrency().getSymbol()))) {
                    String amount = NumberFormat.getCurrencyInstance().getCurrency().getSymbol() + s.toString();
                    rate.setText(amount);
                    rate.setSelection(amount.length());
                }
            }
        });

        //add the currency symbol to the due field
        due.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty() && !(s.toString().startsWith(NumberFormat.getCurrencyInstance().getCurrency().getSymbol()))) {
                    String amount = NumberFormat.getCurrencyInstance().getCurrency().getSymbol() + s.toString();
                    due.setText(amount);
                    due.setSelection(amount.length());
                }
            }
        });

        //now saving
        final Button save = findViewById(R.id.saveNewPatient);

        Log.d("EDIT", String.valueOf(getIntent().getBooleanExtra("EDIT",false)));

        //load previous content if editing
        if(getIntent().getBooleanExtra("EDIT",false)) {
            String id = getIntent().getStringExtra(MainActivity.PATIENT_UID);
            Log.d("EDIT", getIntent().getStringExtra(MainActivity.PATIENT_UID));
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection("patients")
                    .document(getIntent().getStringExtra(MainActivity.PATIENT_UID))
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if(!documentSnapshot.exists()){
                                Snackbar.make(save,"Something went wrong!",Snackbar.LENGTH_INDEFINITE).show();
                                return;
                            }
                            if (documentSnapshot.contains("Name"))
                                patientNameEditText.setText(documentSnapshot.get("Name").toString());
                            if (documentSnapshot.contains("Phone"))
                                contactNumberEditText.setText(documentSnapshot.get("Phone").toString());
                            if (documentSnapshot.contains("Disease"))
                                disease.setText(documentSnapshot.get("Disease").toString());
                            if (documentSnapshot.contains("Rate"))
                                rate.setText(documentSnapshot.get("Rate").toString());
                            if (documentSnapshot.contains("Due"))
                                due.setText(documentSnapshot.get("Due").toString());
                            save.setText("Update");
                        }
                    });
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //error checking data
                if(patientNameEditText.getText().toString().isEmpty()){
                    patientNameEditText.setError(getString(R.string.name_cannot_be_blank));
                    return;
                }
                //firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                //creaating map of all data
                Map<String,Object> patient = new HashMap<>();
                patient.put("Name",patientNameEditText.getText().toString());
                patient.put("Phone",contactNumberEditText.getText().toString());
                patient.put("Disease",disease.getText().toString());
                if(!rate.getText().toString().isEmpty())
                    patient.put("Rate",Double.parseDouble(rate.getText().toString().substring(1)));
                if(!due.getText().toString().isEmpty())
                    patient.put("Due",Double.parseDouble(due.getText().toString().substring(1)));
                patient.put("Date", Calendar.getInstance().getTime());

                //creating docref for new or edited patient record
                DocumentReference documentReference;
                if(getIntent().getBooleanExtra("EDIT",false))
                    documentReference = db.collection("users")
                            .document(FirebaseAuth.getInstance().getUid())
                            .collection("patients")
                            .document(getIntent().getStringExtra(MainActivity.PATIENT_UID));
                else
                    documentReference = db.collection("users")
                            .document(FirebaseAuth.getInstance().getUid())
                            .collection("patients")
                            .document(String.valueOf(Calendar.getInstance().getTimeInMillis()));

                //adding data to document
                documentReference.set(patient)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d("FIRESTORE","Saved patient data");
                                }
                                else {
                                    Log.d("FIRESTORE","Error : "+task.getException().getMessage());
                                }
                            }
                        });
                //finally opening the relevant patient detail view
                Intent openPatientDetail = new Intent(NewPatient.this, PatientView.class);
                openPatientDetail.putExtra(MainActivity.PATIENT_UID,documentReference.getId());
                Log.d("PATIENT UID",documentReference.getId());
                openPatientDetail.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(openPatientDetail);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case READ_CONTACT_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //permission to read contacts granted
                    Log.d("CONTACT PERMISSION","PERMISSION GRANTED!");
                    launchContactPicker();
                }
                else {
                    //permission not granted... disable feature and show prompt
                    Log.d("CONTACT PERMISSION","PERMISSION DENIED!");
                }
        }
    }
    public void launchContactPicker(){
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContactIntent,CONTACT_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CONTACT_PICKER_REQUEST_CODE:
                if (resultCode==RESULT_OK){
                    //when we get the contact. to get the contact name and number
                    Log.d("CONTACT PICKED","RESULT OK");
                    //result successful
                    Uri ContactData = data.getData();
                    Cursor c = getContentResolver().query(ContactData,null,null,null,null);
                    if(c!=null && c.moveToFirst()){

                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        Log.d("CONTACT PICKED","ID = "+id);
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        Log.d("CONTACT PICKED",name);
                        patientNameEditText.setText(name);
                        if(hasPhone.equals("1")){
                            Log.d("CONTACT PICKED",hasPhone);
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " = "+id,null,null);
                            phones.moveToFirst();
                            String phNo = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            Log.d("CONTACT PICKED",phNo);
                            contactNumberEditText.setText(phNo.trim());
                            c.close();
                        }
                    }
                }
        }
    }
}
