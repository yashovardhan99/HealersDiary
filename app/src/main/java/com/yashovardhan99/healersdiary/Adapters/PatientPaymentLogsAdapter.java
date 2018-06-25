package com.yashovardhan99.healersdiary.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yashovardhan99.healersdiary.Objects.PaymentSnapshot;
import com.yashovardhan99.healersdiary.R;

import java.util.ArrayList;

/**
 * Created by Yashovardhan99 on 25-06-2018 as a part of HealersDiary.
 */
public class PatientPaymentLogsAdapter extends RecyclerView.Adapter<PatientPaymentLogsAdapter.ViewHolder> {

    private static ArrayList<PaymentSnapshot> paymentSnapshots;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView mDateView;
        TextView mAmountView;
        public ViewHolder(RelativeLayout v) {
            super(v);
            mDateView = v.findViewById(R.id.paymentDate);
            mAmountView = v.findViewById(R.id.paymentAmount);
        }
    }

    public PatientPaymentLogsAdapter(ArrayList<PaymentSnapshot> mPaymentSnapshots){ paymentSnapshots = mPaymentSnapshots;}

    @NonNull
    @Override
    public PatientPaymentLogsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RelativeLayout v = (RelativeLayout) (LayoutInflater.from(parent.getContext())
                .inflate(R.layout.payment_logs_recycler, parent, false));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mAmountView.setText(paymentSnapshots.get(position).getAmount());
        holder.mDateView.setText(paymentSnapshots.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        if(paymentSnapshots!=null)
            return paymentSnapshots.size();
        else
            return 0;
    }

}
