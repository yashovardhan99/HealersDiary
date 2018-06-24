package com.yashovardhan99.healersdiary.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yashovardhan99.healersdiary.R;

import java.util.ArrayList;

/**
 * Created by Yashovardhan99 on 24-06-2018 as a part of HealersDiary.
 */
public class HealingLogAdapter extends RecyclerView.Adapter<HealingLogAdapter.ViewHolder> {

    private static ArrayList<String> healingLog;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView mTextView;
        public ViewHolder(RelativeLayout v) {
            super(v);
            mTextView = v.findViewById(R.id.healingDetail);
        }
    }

    public HealingLogAdapter(ArrayList<String> mHealingLog){ healingLog = mHealingLog;}

    @NonNull
    @Override
    public HealingLogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RelativeLayout v = (RelativeLayout) (LayoutInflater.from(parent.getContext())
                .inflate(R.layout.healing_log_recycler_view, parent, false));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTextView.setText(healingLog.get(position));
    }

    @Override
    public int getItemCount() {
        if(healingLog!=null)
            return healingLog.size();
        else
            return 0;
    }
}
