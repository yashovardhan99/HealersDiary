package com.yashovardhan99.healersdiary.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yashovardhan99.healersdiary.Activities.PatientView;
import com.yashovardhan99.healersdiary.Objects.Patient;
import com.yashovardhan99.healersdiary.R;

import java.util.ArrayList;

/**
 * Created by Yashovardhan99 on 22-05-2018 as a part of HealersDiary.
 */
public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.ViewHolder> {

    private static ArrayList<Patient> patientList;
    private static ArrayList<Patient> selectedPatients;
    //patient array list
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    static ActionMode mActionMode;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ViewHolder(final RelativeLayout v){
            super(v);
            selectedPatients = new ArrayList<>();
            mTextView = v.findViewById(R.id.patientName);

            final AbsListView.MultiChoiceModeListener actionModeCallbacks = new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    if(checked)
                        selectedPatients.add(patientList.get(position));
                    else
                        selectedPatients.remove(patientList.get(position));
                    if(selectedPatients.isEmpty())
                        mActionMode.finish();
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    menu.add("Delete");
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    mActionMode = null;
                }
            };
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mActionMode!=null) {
                        if(v.isSelected()){
                            v.setSelected(false);
                            v.setBackgroundColor(Color.WHITE);
                            return;
                        }
                        v.setBackgroundColor(Color.LTGRAY);
                        v.setSelected(true);
                        return;
                    }
                    //start patient detail
                    Intent detail = new Intent(context, PatientView.class);
                    detail.putExtra("PATIENT_UID", patientList.get(getAdapterPosition()).getUid());
                    context.startActivity(detail);
                }
            });
            v.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick (View view){
                    if(mActionMode!=null)
                        return false;
                    mActionMode = ((AppCompatActivity) view.getContext()).startActionMode(actionModeCallbacks);
                    Log.d("ACTION_MODE",mActionMode.toString());
                    view.setSelected(true);
                    view.setBackgroundColor(Color.LTGRAY);
                    return true;
                }
            });
        }
    }

    public MainListAdapter(ArrayList<Patient> mPatientList) {
        patientList = mPatientList;
    }

    @NonNull
    @Override
    public MainListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RelativeLayout v = (RelativeLayout) (LayoutInflater.from(parent.getContext())
        .inflate(R.layout.main_recycler_view,parent,false));
        ViewHolder vh = new ViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTextView.setText(patientList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        //keep this like this. It needs to be done for the initial launch
        if (patientList!=null)
            return patientList.size();
        else
            return 0;
    }

}
