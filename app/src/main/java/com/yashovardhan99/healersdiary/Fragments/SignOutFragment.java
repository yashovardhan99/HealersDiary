package com.yashovardhan99.healersdiary.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.yashovardhan99.healersdiary.Activities.Login;
import com.yashovardhan99.healersdiary.R;

/**
 * Created by Yashovardhan99 on 15-08-2018 as a part of HealersDiary.
 */
public class SignOutFragment extends DialogFragment {
    public SignOutFragment(){
        //mandatory empty constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_power_settings_new_black_24dp)
                .setTitle(R.string.sign_out)
                .setMessage("This will sign you out of the app. If you are connected to the internet, your data has been synced online and will not be deleted.")
                .setPositiveButton(R.string.sign_out, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getActivity(), Login.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
                        getActivity().finishAffinity();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                }).create();
    }
}
