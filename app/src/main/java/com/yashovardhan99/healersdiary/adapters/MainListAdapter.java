package com.yashovardhan99.healersdiary.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yashovardhan99.healersdiary.R;
import com.yashovardhan99.healersdiary.activities.MainActivity;
import com.yashovardhan99.healersdiary.activities.PatientView;
import com.yashovardhan99.healersdiary.objects.Patient;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by Yashovardhan99 on 22-05-2018 as a part of HealersDiary.
 */
public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.ViewHolder> {

    private static ArrayList<Patient> patientList;
    //patient array list
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    SharedPreferences preferences;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        TextView patentNameTextView, patientDataTextView;
        ViewHolder(RelativeLayout v){
            super(v);
            patentNameTextView = v.findViewById(R.id.patientName);
            patientDataTextView = v.findViewById(R.id.patientData);
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
            menu.setHeaderTitle(patentNameTextView.getText().toString());
            menu.add(getAdapterPosition(), 0, 0, R.string.edit);
            menu.add(getAdapterPosition(), 1, 0, R.string.delete);
        }
    }

    public MainListAdapter(ArrayList<Patient> mPatientList, SharedPreferences preferences) {
        patientList = mPatientList;
        this.preferences = preferences;
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
        holder.patentNameTextView.setText(patientList.get(position).getName());
        Resources res = context.getResources();
        if(holder.patientDataTextView.getVisibility() == View.GONE)
            holder.patientDataTextView.setVisibility(View.VISIBLE);
        switch (preferences.getInt(MainActivity.MAIN_LIST_CHOICE,0)){
            case 0:
                int healingsToday = patientList.get(position).getHealingsToday();
                holder.patientDataTextView.setText(res.getQuantityString(R.plurals.healing, healingsToday, healingsToday, res.getString(R.string.today)));
                break;
            case 1:
                String famt = res.getString(R.string.payment_due)+": "+NumberFormat.getCurrencyInstance().format(patientList.get(position).getDue());
                holder.patientDataTextView.setText(famt);
                break;
            case 2:
                String frate = res.getString(R.string.rate)+": "+NumberFormat.getCurrencyInstance().format(patientList.get(position).getRate());
                holder.patientDataTextView.setText(frate);
                break;
            case 3:
                holder.patientDataTextView.setText(patientList.get(position).getDisease());
                if(patientList.get(position).getDisease().isEmpty())
                    holder.patientDataTextView.setVisibility(View.GONE);
                break;
        }
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
