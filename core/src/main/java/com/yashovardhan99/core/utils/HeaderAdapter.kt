package com.yashovardhan99.core.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.core.R
import com.yashovardhan99.core.databinding.HeaderLogsBinding

class HeaderAdapter : ListAdapter<String, HeaderAdapter.HeaderViewHolder>(StringDiffCallback()) {
    class HeaderViewHolder(val binding: HeaderLogsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(heading: String) {
            binding.heading = heading
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        return HeaderViewHolder(
            HeaderLogsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.header_logs
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class StringDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}
