package com.yashovardhan99.healersdiary.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.activities.MainActivity
import com.yashovardhan99.healersdiary.activities.PatientView
import com.yashovardhan99.healersdiary.databinding.MainRecyclerViewBinding
import com.yashovardhan99.healersdiary.objects.Patient
import java.text.NumberFormat
import java.util.*

/**
 * Created by Yashovardhan99 on 22-05-2018 as a part of HealersDiary.
 */
class MainListAdapter(private val patientList: ArrayList<Patient>, private val preferences: SharedPreferences) : RecyclerView.Adapter<MainListAdapter.ViewHolder>() {

    lateinit var context: Context

    inner class ViewHolder(val binding: MainRecyclerViewBinding) : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
        var patientDataTextView: TextView = binding.patientData

        init {
            binding.root.setOnClickListener {
                //start patient detail
                val detail = Intent(context, PatientView::class.java)
                detail.putExtra("PATIENT_UID", patientList[adapterPosition].uid)
                context.startActivity(detail)
            }

            binding.root.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.setHeaderTitle(binding.patient?.name)
            menu?.add(adapterPosition, 0, 0, R.string.edit)
            menu?.add(adapterPosition, 1, 0, R.string.delete)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = MainRecyclerViewBinding.inflate(LayoutInflater.from(context),
                parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.patient = patientList[position]
        val res = context.resources
        if (holder.patientDataTextView.visibility == View.GONE)
            holder.patientDataTextView.visibility = View.VISIBLE
        when (preferences.getInt(MainActivity.MAIN_LIST_CHOICE, 0)) {
            0 -> {
                val healingsToday = patientList[position].healingsToday
                holder.patientDataTextView.text = res.getQuantityString(R.plurals.healing, healingsToday, healingsToday, res.getString(R.string.today))
            }
            1 -> {
                val famt = res.getString(R.string.payment_due) + ": " + NumberFormat.getCurrencyInstance().format(patientList[position].due)
                holder.patientDataTextView.text = famt
            }
            2 -> {
                val frate = res.getString(R.string.rate) + ": " + NumberFormat.getCurrencyInstance().format(patientList[position].rate)
                holder.patientDataTextView.text = frate
            }
            3 -> {
                holder.patientDataTextView.text = patientList[position].disease
                if (patientList[position].disease.isEmpty())
                    holder.patientDataTextView.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        //keep this like this. It needs to be done for the initial launch
        return patientList.size
    }
}
