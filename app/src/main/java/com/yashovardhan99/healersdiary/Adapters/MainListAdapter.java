package com.yashovardhan99.healersdiary.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    //patient array list
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        final TextView mTextView;
        ViewHolder(RelativeLayout v){
            super(v);
            mTextView = v.findViewById(R.id.patientName);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //start patient detail
                    Intent detail = new Intent(context, PatientView.class);
                    detail.putExtra("PATIENT_UID",patientList.get(getAdapterPosition()).getUid());
                    context.startActivity(detail);
                }
            });
            v.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(mTextView.getText().toString());
            menu.add(getAdapterPosition(), 0, 0, R.string.edit);
            menu.add(getAdapterPosition(), 1, 0, R.string.delete);
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
