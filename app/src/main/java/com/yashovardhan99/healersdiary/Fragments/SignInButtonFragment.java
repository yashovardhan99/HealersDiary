package com.yashovardhan99.healersdiary.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.SignInButton;
import com.yashovardhan99.healersdiary.Activities.Login;
import com.yashovardhan99.healersdiary.R;

public class SignInButtonFragment extends Fragment {
    public SignInButtonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SignInButton gsb = getView().findViewById(R.id.GoogleSignInButton);
        gsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = ((Login)getActivity()).getmGoogleSignInClient().getSignInIntent();
                startActivityForResult(signInIntent,Login.GOOGLE_SIGN_IN_RC);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in_button, container, false);
    }
}
