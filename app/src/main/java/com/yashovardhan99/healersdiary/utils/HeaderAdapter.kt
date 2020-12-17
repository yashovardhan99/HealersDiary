package com.yashovardhan99.healersdiary.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.HeaderLogsBinding

class HeaderAdapter(var isVisible: Boolean) : RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {
    class HeaderViewHolder(val context: Context, val binding: HeaderLogsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.heading = context.resources.getString(R.string.recent_activity)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        return HeaderViewHolder(parent.context, HeaderLogsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return if (isVisible) 1 else 0
    }
}

