package com.yashovardhan99.healersdiary.fragments;

import android.content.Intent;
import android.net.Uri;
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

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.yashovardhan99.healersdiary.BuildConfig;
import com.yashovardhan99.healersdiary.R;

import java.util.Objects;

/**
 * Created by Yashovardhan99 on 18-08-2018 as a part of HealersDiary.
 */
public class AboutFragment extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_about,container,false);
        TextView imageAttrib = RootView.findViewById(R.id.imageCour);
        imageAttrib.setMovementMethod(LinkMovementMethod.getInstance());
        TextView version = RootView.findViewById(R.id.appVersionString);
        version.setText(getString(R.string.version_s, BuildConfig.VERSION_NAME));
        RootView.findViewById(R.id.oss).setOnClickListener(this);
        RootView.findViewById(R.id.privacy).setOnClickListener(this);
        RootView.findViewById(R.id.eula).setOnClickListener(this);

        Toolbar toolbar = RootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle(R.string.about);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }
        return RootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.oss:
                startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
                break;
            case R.id.privacy:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yashovardhan99/HealersDiary/blob/master/PRIVACY%20POLICY.md")));
                break;
            case R.id.eula:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yashovardhan99/HealersDiary/blob/master/EULA.md")));
        }
    }
}
