package com.yashovardhan99.healersdiary.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
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

    public AboutFragment(){
        //mandatory empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_about,container,false);
        TextView imageAttrib = RootView.findViewById(R.id.imageCour);
        imageAttrib.setMovementMethod(LinkMovementMethod.getInstance());
        TextView version = RootView.findViewById(R.id.appVersionString);
        version.setText(getString(R.string.version_s, BuildConfig.VERSION_NAME));
        ((AppBarLayout)getActivity().findViewById(R.id.appBarLayout)).setExpanded(false);
        return RootView;
    }
}
