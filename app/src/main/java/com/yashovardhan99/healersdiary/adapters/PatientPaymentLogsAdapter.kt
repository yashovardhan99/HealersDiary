package com.yashovardhan99.healersdiary.adapters

import android.content.Context
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.objects.PaymentSnapshot
import java.util.*

/**
 * Created by Yashovardhan99 on 25-06-2018 as a part of HealersDiary.
 */
class PatientPaymentLogsAdapter(private val paymentSnapshots: ArrayList<PaymentSnapshot>) : RecyclerView.Adapter<PatientPaymentLogsAdapter.ViewHolder>() {

    private lateinit var context: Context

    inner class ViewHolder(v: RelativeLayout) : RecyclerView.ViewHolder(v), View.OnCreateContextMenuListener {
        val mDateView: TextView = v.findViewById(R.id.paymentDate)
        val mAmountView: TextView = v.findViewById(R.id.paymentAmount)

        init {
            v.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.setHeaderTitle(context.getString(R.string.paid_on_date, mAmountView.text, mDateView.text))
            menu?.add(adapterPosition, 0, 0, R.string.edit)
            menu?.add(adapterPosition, 1, 0, R.string.delete)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.payment_logs_recycler, parent, false) as RelativeLayout
        context = parent.context
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mAmountView.text = paymentSnapshots[position].amount
        holder.mDateView.text = paymentSnapshots[position].date
    }

    override fun getItemCount(): Int {
        return paymentSnapshots.size
    }
}
