package com.yashovardhan99.healersdiary.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;

import com.yashovardhan99.healersdiary.Objects.PatientFeedback;

import java.util.ArrayList;

/**
 * Created by Yashovardhan99 on 28-07-2018 as a part of HealersDiary.
 */
public class PatientFeedbackListAdapter extends RecyclerView.Adapter<PatientFeedbackListAdapter.ViewHolder> {

    private static ArrayList<PatientFeedback> patientFeedbacks;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public ViewHolder(View itemView) {

            super(itemView);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        }
    }

    @NonNull
    @Override
    public PatientFeedbackListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull PatientFeedbackListAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
