package com.yashovardhan99.healersdiary.adapters

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.objects.Healing
import java.util.*

/**
 * Created by Yashovardhan99 on 24-06-2018 as a part of HealersDiary.
 */
class HealingLogAdapter(private val healingLog: ArrayList<Healing>) : RecyclerView.Adapter<HealingLogAdapter.ViewHolder>() {

    class ViewHolder(v: RelativeLayout) : RecyclerView.ViewHolder(v), View.OnCreateContextMenuListener {
        val mTextView: TextView = v.findViewById(R.id.healingDetail)

        init {
            v.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.setHeaderTitle(mTextView.text)
            menu?.add(adapterPosition, 0, 0, "Delete")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.healing_log_recycler_view, parent, false) as RelativeLayout
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mTextView.text = healingLog[position].date
    }

    override fun getItemCount(): Int {
        return healingLog.size
    }
}
