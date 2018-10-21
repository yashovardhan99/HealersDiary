package com.yashovardhan99.healersdiary.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.yashovardhan99.healersdiary.R;
import com.yashovardhan99.healersdiary.activities.MainActivity;

/**
 * Created by Yashovardhan99 on 20/10/18 as a part of HealersDiary.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, CompoundButton.OnCheckedChangeListener {

    LinearLayout mainListChoice;
    TextView selectedMainListChoice;
    Switch crashSwitch;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_settings,container,false);
        mainListChoice = RootView.findViewById(R.id.ChooseMainListPref);
        selectedMainListChoice = RootView.findViewById(R.id.chosenMainListPref);
        crashSwitch = RootView.findViewById(R.id.CrashSwitch);
        crashSwitch.setOnCheckedChangeListener(this);
        crashSwitch.setChecked(preferences.getBoolean(MainActivity.CRASH_ENABLED,true));

        String selectedText;
        switch(preferences.getInt(MainActivity.MAIN_LIST_CHOICE,0)){
            case 1: selectedText = getString(R.string.payment_due);
                break;
            case 2: selectedText = getString(R.string.rate);
                break;
            case 3: selectedText = getString(R.string.disease);
                break;
            default: selectedText = getString(R.string.number_of_healings_today);
        }
        selectedMainListChoice.setText(selectedText);

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
        editor = preferences.edit();
        switch (item.getGroupId()){
            case R.id.mainDisplayPrefGroup:
                selectedMainListChoice.setText(item.getTitle());
                editor.putInt(MainActivity.MAIN_LIST_CHOICE, item.getOrder());
                editor.apply();
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor = preferences.edit();
        switch (buttonView.getId()){
            case R.id.CrashSwitch:
                editor.putBoolean(MainActivity.CRASH_ENABLED,isChecked);
                if(isChecked){
                    ((MainActivity)getActivity()).setCrashEnabled();
                }
                else
                    Toast.makeText(getActivity(),"Please restart the app for the change to take effect",Toast.LENGTH_SHORT).show();
                editor.apply();
                break;
        }
    }
}
