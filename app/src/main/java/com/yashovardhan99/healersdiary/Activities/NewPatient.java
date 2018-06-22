package com.yashovardhan99.healersdiary.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yashovardhan99.healersdiary.R;

public class NewPatient extends AppCompatActivity {

    final int READ_CONTACT_PERMISSION_REQUEST_CODE = 1;
    final int CONTACT_PICKER_REQUEST_CODE = 2;
    TextInputEditText patientNameEditText;
    TextInputEditText contactNumberEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient);

         patientNameEditText = findViewById(R.id.patientName);
        contactNumberEditText = findViewById(R.id.phoneNumber);

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
                        }
                    }
                }
        }
    }
}
