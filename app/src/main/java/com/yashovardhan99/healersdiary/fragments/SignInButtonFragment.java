package com.yashovardhan99.healersdiary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.SignInButton;
import com.yashovardhan99.healersdiary.activities.Login;
import com.yashovardhan99.healersdiary.R;

public class SignInButtonFragment extends Fragment {

    public SignInButtonFragment() {
        // Required empty public constructor
    }

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View RootView = inflater.inflate(R.layout.fragment_sign_in_button, container, false);
        SignInButton gsb = RootView.findViewById(R.id.GoogleSignInButton);
        gsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Login)getActivity()).signInWithGoogle();
            }
        });

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
