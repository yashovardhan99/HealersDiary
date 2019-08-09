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
import com.yashovardhan99.healersdiary.objects.PatientFeedback
import java.text.DateFormat
import java.util.*

/**
 * Created by Yashovardhan99 on 28-07-2018 as a part of HealersDiary.
 */
class PatientFeedbackListAdapter(private val patientFeedbacks: ArrayList<PatientFeedback>) : RecyclerView.Adapter<PatientFeedbackListAdapter.ViewHolder>() {

    private lateinit var context: Context

    inner class ViewHolder(v: RelativeLayout) : RecyclerView.ViewHolder(v), View.OnCreateContextMenuListener {
        val mDateView: TextView = v.findViewById(R.id.PatientFeedbackDateText)
        val mFeedbackView: TextView = v.findViewById(R.id.PatientFeedbackText)

        init {
            v.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.setHeaderTitle(context.getString(R.string.paid_on_date, context.getString(R.string.feedback), mDateView.text))
            menu?.add(adapterPosition, 0, 0, R.string.edit)
            menu?.add(adapterPosition, 1, 0, R.string.delete)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.patient_feedback_list_adapter, parent, false) as RelativeLayout
        context = parent.context
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mFeedbackView.text = patientFeedbacks[position].feedback
        holder.mDateView.text = DateFormat.getDateInstance().format(patientFeedbacks[position].timestamp.toDate())
    }

    override fun getItemCount(): Int {
        return patientFeedbacks.size
    }
}
