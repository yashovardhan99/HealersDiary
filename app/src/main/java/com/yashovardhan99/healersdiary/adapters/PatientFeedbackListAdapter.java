package com.yashovardhan99.healersdiary.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yashovardhan99.healersdiary.objects.PatientFeedback;
import com.yashovardhan99.healersdiary.R;

import java.util.ArrayList;

/**
 * Created by Yashovardhan99 on 28-07-2018 as a part of HealersDiary.
 */
public class PatientFeedbackListAdapter extends RecyclerView.Adapter<PatientFeedbackListAdapter.ViewHolder> {

    private static Context context;
    private static ArrayList<PatientFeedback> patientFeedbacks;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        final TextView mDateView;
        final TextView mFeedbackView;
        public ViewHolder(RelativeLayout v) {
            super(v);
            mDateView = v.findViewById(R.id.PatientFeedbackDateText);
            mFeedbackView = v.findViewById(R.id.PatientFeedbackText);
            v.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(context.getString(R.string.paid_on_date, context.getString(R.string.feedback), mDateView.getText()));
            menu.add(getAdapterPosition(), 0, 0, R.string.edit);
            menu.add(getAdapterPosition(), 1, 0, R.string.delete);
        }
    }

    public PatientFeedbackListAdapter(ArrayList<PatientFeedback> mPatientFeedbacks) {
        patientFeedbacks = mPatientFeedbacks;
    }

    @NonNull
    @Override
    public PatientFeedbackListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RelativeLayout v = (RelativeLayout) (LayoutInflater.from(parent.getContext())
        .inflate(R.layout.patient_feedback_list_adapter, parent, false));
        context = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientFeedbackListAdapter.ViewHolder holder, int position) {
        holder.mFeedbackView.setText(patientFeedbacks.get(position).getFeedback());
        holder.mDateView.setText(patientFeedbacks.get(position).getTimestamp());
    }

    @Override
    public int getItemCount() {
        if(patientFeedbacks!=null)
            return patientFeedbacks.size();
        else
            return 0;
    }
}
