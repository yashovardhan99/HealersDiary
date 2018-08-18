package com.yashovardhan99.healersdiary.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yashovardhan99.healersdiary.Fragments.AboutFragment;
import com.yashovardhan99.healersdiary.Fragments.MainListFragment;
import com.yashovardhan99.healersdiary.Fragments.ProFragment;
import com.yashovardhan99.healersdiary.Fragments.SignOutFragment;
import com.yashovardhan99.healersdiary.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    public static final String PATIENT_UID = "PATIENT_UID";
    private static final String PATIENT_RECORD = "patient_record";
    private static final String DELETE_BUTTON = "Delete Button";
    private static final String EDIT_BUTTON = "Edit Button";
    private static final String NEW = "New";
    public static final String USERS = "users";
    public static final String FIRESTORE = "FIRESTORE";
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static int healingsToday;
    private static int healingsYesterday;
    public static BillingClient mBillingClient;
    private MainListFragment listFragment;
    private ProFragment proFragment;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private AboutFragment aboutFragment;
    private Fragment mContent;
    private String FRAG_KEY = "MY_FRAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mainActivityToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mainActivityToolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.main_nav_view);

        final ImageView profilePic = mNavigationView.getHeaderView(0).findViewById(R.id.profilePic);
        final TextView profileName = mNavigationView.getHeaderView(0).findViewById(R.id.profileName);

        //check login and handle
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            Log.d("SIGN","NO USER");
            //not signed in
            startActivity(new Intent(this, Login.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
            finishAffinity();
            return;
        }


        listFragment = new MainListFragment();
        proFragment = new ProFragment();
        aboutFragment = new AboutFragment();

        if(savedInstanceState!=null){
            mContent = getSupportFragmentManager().getFragment(savedInstanceState,FRAG_KEY);
            getSupportFragmentManager().beginTransaction().replace(R.id.mainListHolder, mContent).commit();
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.mainListHolder, listFragment).commit();
            mContent = listFragment;
        }
        resetHealingCounters();


        Uri profilePicture = mUser.getPhotoUrl();

        Picasso.get().load(profilePicture).placeholder(R.drawable.ic_person_black_24dp).centerCrop().fit().into(profilePic, new Callback() {
            @Override
            public void onSuccess() {
                Bitmap imageBit = ((BitmapDrawable) profilePic.getDrawable()).getBitmap();
                RoundedBitmapDrawable bitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBit);
                bitmapDrawable.setCircular(true);
                bitmapDrawable.setCornerRadius(Math.max(imageBit.getWidth(), imageBit.getHeight())/2.0f);
                profilePic.setImageDrawable(bitmapDrawable);
            }

            @Override
            public void onError(Exception e) {
            }
        });


        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getGroupId()==R.id.MainNavGroup){
                    item.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    switch (item.getItemId()){
                        case R.id.home:
//                            getSupportFragmentManager().beginTransaction().replace(R.id.mainListHolder, listFragment).commit();
                            mContent = listFragment;
                            break;

                        case R.id.getPro:
                            mContent = proFragment;
//                            getSupportFragmentManager().beginTransaction().replace(R.id.mainListHolder, proFragment).commit();
                            break;

                        case R.id.about:
                            mContent = aboutFragment;
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainListHolder, mContent).commit();
                    return true;
                }
                else{
                    switch (item.getItemId()){
                        case R.id.shareMenuItem:
                            share();
                            return true;
                        case R.id.signOutMenuItem:
                            //add alertbox
                            signOut();
                            return true;
                    }
                }
                return true;
            }
        });

        //display welcome message
        if (mUser.getDisplayName() != null)
            profileName.setText(mUser.getDisplayName());

        //firestore init
        db = FirebaseFirestore.getInstance();


        ////new patient record button
        FloatingActionButton newPatientButton = findViewById(R.id.new_fab);
        newPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPatinetIntent = new Intent(MainActivity.this, NewPatient.class);
                startActivity(newPatinetIntent);
                //log firebase analytics event
                Bundle newPatient = new Bundle();
                newPatient.putString(FirebaseAnalytics.Param.LOCATION, MainActivity.class.getName());
                newPatient.putString(FirebaseAnalytics.Param.CONTENT_TYPE, NEW);
                newPatient.putString(FirebaseAnalytics.Param.ITEM_CATEGORY,PATIENT_RECORD);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, newPatient);
            }
        });
    }

    public void resetHealingCounters() {
        //initialize healings counter
        healingsToday = 0;
        healingsYesterday = 0;
    }

    private void signOut() {
        DialogFragment signOutFragment = new SignOutFragment();
        signOutFragment.show(getSupportFragmentManager(),"SIGNOUT");
//        startActivity(new Intent(MainActivity.this, Login.class)
//                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
//        finishAffinity();
    }


    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        String title = item.getTitle().toString();
        final String EDIT = getString(R.string.edit);
        final String DELETE = getString(R.string.delete);
        if (title.equals(EDIT)) {
            //edit patient data
            Intent editPatient = new Intent(this, NewPatient.class);
            editPatient.putExtra("EDIT", true);
            String id = listFragment.patientList.get(item.getGroupId()).getUid();
            editPatient.putExtra(PATIENT_UID, id);
            startActivity(editPatient);

            //log analytics
            Bundle edit = new Bundle();
            edit.putString(FirebaseAnalytics.Param.LOCATION, MainActivity.class.getName());
            edit.putString(FirebaseAnalytics.Param.CONTENT_TYPE, EDIT_BUTTON);
            edit.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, PATIENT_RECORD);
            edit.putString(FirebaseAnalytics.Param.ITEM_ID, id);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, edit);

            return true;
        } else if (title.equals(DELETE)) {
            //delete patient data
            final AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(MainActivity.this);
            confirmBuilder.setMessage(R.string.delete_warning_message)
                    .setTitle(R.string.sure_q)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DeletePatientRecord(item.getGroupId());
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //action cancelled
                        }
                    });
            //to confirm deletion
            AlertDialog confirm = confirmBuilder.create();
            confirm.show();
            return true;
        } else
            return super.onContextItemSelected(item);
    }

    private void DeletePatientRecord(int id) {
        DocumentReference patient = db.collection(USERS)
                .document(Objects.requireNonNull(mAuth.getUid()))
                .collection("patients")
                .document(listFragment.patientList.get(id).getUid());
        //now to delete this record, we first delete all healing and payment history of this patient
        final CollectionReference healings = patient
                .collection("healings");
        healings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        documentSnapshot.getReference().delete();
                    }
                }
            }
        });

        CollectionReference payments = patient.collection("payments");
        payments.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        documentSnapshot.getReference().delete();
                    }
                }
            }
        });

        //now deleting patient document
        patient.delete();
        Snackbar.make(findViewById(R.id.recycler_main), R.string.deleted, Snackbar.LENGTH_LONG).show();

        //logging in analytics
        Bundle delete = new Bundle();
        delete.putString(FirebaseAnalytics.Param.CONTENT_TYPE, DELETE_BUTTON);
        delete.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, PATIENT_RECORD);
        delete.putString(FirebaseAnalytics.Param.LOCATION,MainActivity.class.getName());
        delete.putString(FirebaseAnalytics.Param.ITEM_ID,patient.getId());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, delete);
    }

    public void countHealings(String uid) {
        Log.d("COUNTING HEALINGS", uid);

        //yesterday's timestamp
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -2);
        Date yest = yesterday.getTime();
        Timestamp y = new Timestamp(yest);

        db.collection(USERS)
                .document(Objects.requireNonNull(mAuth.getUid()))
                .collection("patients")
                .document(uid)
                .collection("healings")
                .orderBy("Date", Query.Direction.DESCENDING)
                .whereGreaterThan("Date",y)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                         if(queryDocumentSnapshots == null)
                             return;
                        for(DocumentChange dc:queryDocumentSnapshots.getDocumentChanges()){
                            Timestamp timestamp = dc.getDocument().getTimestamp("Date");
                            if(timestamp==null)
                                continue;
                            Log.d("COUNTING HEALINGS",timestamp.toString());
                            Long time = timestamp.toDate().getTime();
                            switch (dc.getType()){
                                case ADDED:
                                    if(DateUtils.isToday(time))
                                        healingsToday++;
                                    else if(DateUtils.isToday(time+DateUtils.DAY_IN_MILLIS))
                                        healingsYesterday++;
                                    break;
                                case REMOVED:
                                    if(DateUtils.isToday(time))
                                        healingsToday--;
                                    else if(DateUtils.isToday(time+DateUtils.DAY_IN_MILLIS))
                                        healingsYesterday--;
                                    break;
                            }
                        }
                        updateTextFields();
                    }
                });
    }
    private void updateTextFields(){
        TextView today = findViewById(R.id.healingsToday);
        TextView yesterday = findViewById(R.id.healingsYesterday);
        Resources res = getResources();
        today.setText(res.getQuantityString(R.plurals.healing, healingsToday, healingsToday, getString(R.string.today)));
        yesterday.setText(res.getQuantityString(R.plurals.healing, healingsYesterday, healingsYesterday, getString(R.string.yesterday)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.shareMenuItem:
                share();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void share() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT,getString(R.string.app_name));
        share.setType("text/plain");
        startActivity(Intent.createChooser(share,getString(R.string.share)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options_menu,menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FRAG_KEY, mContent);
    }
}