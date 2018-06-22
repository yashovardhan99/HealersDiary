package com.yashovardhan99.healersdiary.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yashovardhan99.healersdiary.Activities.MainActivity;
import com.yashovardhan99.healersdiary.Activities.PatientView;
import com.yashovardhan99.healersdiary.R;

/**
 * Created by Yashovardhan99 on 22-05-2018 as a part of HealersDiary.
 */
public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.ViewHolder> {

    private String[] patientList;
    static Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ViewHolder(RelativeLayout v){
            super(v);
            context = v.getContext();
            mTextView = v.findViewById(R.id.patientName);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context,("TEST"+getAdapterPosition()),Toast.LENGTH_LONG).show();
                    Intent detail = new Intent(context, PatientView.class);
                    context.startActivity(detail);
                }
            });
        }
    }

    public MainListAdapter(String[] mPatientList) {
        patientList = mPatientList;
    }

    @Override
    public MainListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout v = (RelativeLayout) (LayoutInflater.from(parent.getContext())
        .inflate(R.layout.main_recycler_view,parent,false));
        ViewHolder vh = new ViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTextView.setText(patientList[position]);
    }

    @Override
    public int getItemCount() {
        return patientList.length;
    }
}
