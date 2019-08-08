package com.yashovardhan99.healersdiary.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
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
import com.yashovardhan99.healersdiary.R;
import com.yashovardhan99.healersdiary.adapters.MainListAdapter;
import com.yashovardhan99.healersdiary.fragments.AboutFragment;
import com.yashovardhan99.healersdiary.fragments.DonateFragment;
import com.yashovardhan99.healersdiary.fragments.MainListFragment;
import com.yashovardhan99.healersdiary.fragments.SettingsFragment;
import com.yashovardhan99.healersdiary.fragments.SignOutFragment;
import com.yashovardhan99.healersdiary.objects.Patient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import javax.annotation.Nullable;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String PATIENT_UID = "PATIENT_UID";
    public static final String BILL_TYPE = "billtype";
    public static final String PATIENT_RECORD = "patient_record";
    public static final String NEW = "New";
    public static final String MAIN_LIST_CHOICE = "MAIN_LIST_CHOICE";
    public static final String CRASH_ENABLED = "CRASH_REPORTING_STATE";
    public static final String USERS = "users";
    public static final String FIRESTORE = "FIRESTORE";
    public static final String INCLUDE_LOGS = "includeHealingLogs";
    private static final String DELETE_BUTTON = "Delete Button";
    private static final String EDIT_BUTTON = "Edit Button";
    public static FirebaseAnalytics mFirebaseAnalytics;
    public static int healingsToday;
    public static int healingsYesterday;
    public ArrayList<Patient> patientList;
    EventListener<QuerySnapshot> queryDocumentSnapshots;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MainListFragment listFragment;
    private DonateFragment donateFragment;
    private SettingsFragment settingsFragment;
    private DrawerLayout mDrawerLayout;
    private AboutFragment aboutFragment;
    private Fragment mContent;
    private String FRAG_KEY = "MY_FRAG";
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        resetHealingCounters();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView mNavigationView = findViewById(R.id.main_nav_view);

        final ImageView profilePic = mNavigationView.getHeaderView(0).findViewById(R.id.profilePic);
        final TextView profileName = mNavigationView.getHeaderView(0).findViewById(R.id.profileName);

        //check login and handle
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            Log.d("SIGN", "NO USER");
            //not signed in
            startActivity(new Intent(this, Login.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finishAffinity();
            return;
        }

        listFragment = new MainListFragment();
        donateFragment = new DonateFragment();
        aboutFragment = new AboutFragment();
        settingsFragment = new SettingsFragment();

        if (savedInstanceState != null) {
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, FRAG_KEY);
            getSupportFragmentManager().beginTransaction().replace(R.id.mainListHolder, mContent).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.mainListHolder, listFragment).commit();
            mContent = listFragment;
        }


        Uri profilePicture = mUser.getPhotoUrl();

        Picasso.get().load(profilePicture).placeholder(R.drawable.ic_person_black_24dp).centerCrop().fit().into(profilePic, new Callback() {
            @Override
            public void onSuccess() {
                Bitmap imageBit = ((BitmapDrawable) profilePic.getDrawable()).getBitmap();
                RoundedBitmapDrawable bitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBit);
                bitmapDrawable.setCircular(true);
                bitmapDrawable.setCornerRadius(Math.max(imageBit.getWidth(), imageBit.getHeight()) / 2.0f);
                profilePic.setImageDrawable(bitmapDrawable);
            }

            @Override
            public void onError(Exception e) {
            }
        });


        mNavigationView.setNavigationItemSelectedListener(this);

        //display welcome message
        if (mUser.getDisplayName() != null)
            profileName.setText(mUser.getDisplayName());

        //firestore init
        db = FirebaseFirestore.getInstance();


        //load data from database

        patientList = new ArrayList<>();

        CollectionReference patients = db.collection(MainActivity.USERS)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection("patients");

        //to instantly make any changes reflect here
        queryDocumentSnapshots = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(MainActivity.FIRESTORE, "ERROR : " + e.getMessage());
                    return;
                }
                Log.d(MainActivity.FIRESTORE, "Data fetched");
                if (queryDocumentSnapshots == null)
                    return;
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    //getting changes in documents
                    Log.d(MainActivity.FIRESTORE, dc.getDocument().getData().toString());

                    QueryDocumentSnapshot document = dc.getDocument();

                    switch (dc.getType()) {

                        case ADDED:
                            //add new patient to arrayList
                            Patient patient = new Patient();
                            patient.name = Objects.requireNonNull(document.get("Name")).toString();
                            patient.uid = document.getId();
                            patient.disease = Objects.requireNonNull(document.get("Disease")).toString();
                            if (document.contains("Due") && !document.get("Due").toString().isEmpty()) {
                                patient.due = document.getDouble("Due");
                            }
                            if (document.contains("Rate") && !document.get("Rate").toString().isEmpty()) {
                                patient.rate = document.getDouble("Rate");
                            }
                            patientList.add(patient);
                            mAdapter.notifyItemInserted(patientList.indexOf(patient));
                            countHealings(patient);
                            break;

                        case MODIFIED:
                            //modify patient name
                            String id = dc.getDocument().getId();
                            for (Patient patient1 : patientList) {
                                if (patient1.getUid().equals(id)) {
                                    patient1.name = Objects.requireNonNull(document.get("Name")).toString();
                                    patient1.disease = Objects.requireNonNull(document.get("Disease")).toString();
                                    if (document.contains("Due") && !Objects.requireNonNull(document.get("Due")).toString().isEmpty()) {
                                        patient1.due = document.getDouble("Due");
                                    }
                                    if (document.contains("Rate") && !Objects.requireNonNull(document.get("Rate")).toString().isEmpty()) {
                                        patient1.rate = document.getDouble("Rate");
                                    }
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
        mAdapter = new MainListAdapter(patientList, getPreferences(MODE_PRIVATE));
        if (getPreferences(MODE_PRIVATE).getBoolean(CRASH_ENABLED, true))
            this.setCrashEnabled();
    }

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    public void resetHealingCounters() {
        //initialize healings counter
        healingsToday = 0;
        healingsYesterday = 0;
    }

    private void signOut() {
        DialogFragment signOutFragment = new SignOutFragment();
        signOutFragment.show(getSupportFragmentManager(), "SIGNOUT");
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
            String id = patientList.get(item.getGroupId()).getUid();
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
                .document(patientList.get(id).getUid());
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
        delete.putString(FirebaseAnalytics.Param.LOCATION, MainActivity.class.getName());
        delete.putString(FirebaseAnalytics.Param.ITEM_ID, patient.getId());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, delete);
    }

    public void countHealings(final Patient patient) {
        Log.d("COUNTING HEALINGS", patient.getUid());

        //yesterday's timestamp
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -2);
        Date yest = yesterday.getTime();
        Timestamp y = new Timestamp(yest);

        db.collection(USERS)
                .document(Objects.requireNonNull(mAuth.getUid()))
                .collection("patients")
                .document(patient.getUid())
                .collection("healings")
                .orderBy("Date", Query.Direction.DESCENDING)
                .whereGreaterThan("Date", y)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots == null)
                            return;
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            Timestamp timestamp = dc.getDocument().getTimestamp("Date");
                            if (timestamp == null)
                                continue;
                            Log.d("COUNTING HEALINGS", timestamp.toString());
                            Long time = timestamp.toDate().getTime();
                            switch (dc.getType()) {
                                case ADDED:
                                    if (DateUtils.isToday(time)) {
                                        healingsToday++;
                                        patient.healingsToday++;
                                    } else if (DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS))
                                        healingsYesterday++;
                                    break;
                                case REMOVED:
                                    if (DateUtils.isToday(time)) {
                                        healingsToday--;
                                        patient.healingsToday--;
                                    } else if (DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS))
                                        healingsYesterday--;
                                    break;
                            }
                        }
                        mAdapter.notifyItemChanged(patientList.indexOf(patient));
                        try {
                            listFragment.updateTextFields();
                        } catch (IllegalStateException ise) {
                            Log.d("COUNTING HEALINGS", "UDT NOT CALLED" + ise.toString());
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        share.putExtra(Intent.EXTRA_TEXT, "Healers Diary - A simple and free app to keep track of your patients, healings, payments and more! Download now at https://play.google.com/store/apps/details?id=com.yashovardhan99.healersdiary");
        share.setType("text/plain");
        startActivity(Intent.createChooser(share, getString(R.string.share)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options_menu, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FRAG_KEY, mContent);
    }

    public void setCrashEnabled() {
        Log.d("CRASH", "Enabled");
        Fabric.with(this, new Crashlytics());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getGroupId()) {
            case R.id.MainNavGroup:
                item.setChecked(true);
                mDrawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.home:
                        mContent = listFragment;
                        break;

                    case R.id.getPro:
                        mContent = donateFragment;
                        break;

                    case R.id.about:
                        mContent = aboutFragment;
                        break;
                    case R.id.settings:
                        mContent = settingsFragment;
                        break;
                    default:
                        return false;
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.mainListHolder, mContent);
                if (mContent == listFragment)
                    clearBackStack();
                else
                    transaction.addToBackStack(mContent.getClass().getName());
                transaction.commit();
                return true;
            case R.id.extra_nav_group:
                switch (item.getItemId()) {
                    case R.id.signOutMenuItem:
                        signOut();
                        return true;
                }
                break;
        }
        return false;
    }

    private void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}