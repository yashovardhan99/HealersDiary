package com.yashovardhan99.healersdiary.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.yashovardhan99.healersdiary.R;

/**
 * Created by Yashovardhan99 on 20/10/18 as a part of HealersDiary.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    LinearLayout mainListChoice;
    TextView selectedMainListChoice;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_settings,container,false);
        mainListChoice = RootView.findViewById(R.id.ChooseMainListPref);
        selectedMainListChoice = RootView.findViewById(R.id.chosenMainListPref);
        mainListChoice.setOnClickListener(this);
        return RootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ChooseMainListPref:
                PopupMenu mainListPref = new PopupMenu(getActivity(), selectedMainListChoice);
                mainListPref.getMenuInflater().inflate(R.menu.main_display_preference, mainListPref.getMenu());
                mainListPref.show();
                mainListPref.setOnMenuItemClickListener(this);
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getGroupId()){
            case R.id.mainDisplayPrefGroup:
                selectedMainListChoice.setText(item.getTitle());
                Log.d("MENU",item.getTitle().toString());
        }
        return false;
    }
}
