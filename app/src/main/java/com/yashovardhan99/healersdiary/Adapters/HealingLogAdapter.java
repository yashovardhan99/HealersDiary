package com.yashovardhan99.healersdiary.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yashovardhan99.healersdiary.Objects.Healing;
import com.yashovardhan99.healersdiary.R;

import java.util.ArrayList;

/**
 * Created by Yashovardhan99 on 24-06-2018 as a part of HealersDiary.
 */
public class HealingLogAdapter extends RecyclerView.Adapter<HealingLogAdapter.ViewHolder> {

    private static ArrayList<Healing> healingLog;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        TextView mTextView;
        public ViewHolder(RelativeLayout v) {
            super(v);
            mTextView = v.findViewById(R.id.healingDetail);
            v.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(mTextView.getText());
            menu.add(getAdapterPosition(),0,0,"Delete");
        }
    }

    public HealingLogAdapter(ArrayList<Healing> mHealingLog){ healingLog = mHealingLog;}

    @NonNull
    @Override
    public HealingLogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RelativeLayout v = (RelativeLayout) (LayoutInflater.from(parent.getContext())
                .inflate(R.layout.healing_log_recycler_view, parent, false));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTextView.setText(healingLog.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        if(healingLog!=null)
            return healingLog.size();
        else
            return 0;
    }
}
