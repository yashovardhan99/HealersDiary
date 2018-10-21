package com.yashovardhan99.healersdiary.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yashovardhan99.healersdiary.BuildConfig;
import com.yashovardhan99.healersdiary.R;

/**
 * Created by Yashovardhan99 on 18-08-2018 as a part of HealersDiary.
 */
public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_about,container,false);
        TextView imageAttrib = RootView.findViewById(R.id.imageCour);
        imageAttrib.setMovementMethod(LinkMovementMethod.getInstance());
        TextView version = RootView.findViewById(R.id.appVersionString);
        version.setText(getString(R.string.version_s, BuildConfig.VERSION_NAME));

        Toolbar toolbar = RootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle(R.string.about);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }
        return RootView;
    }
}
