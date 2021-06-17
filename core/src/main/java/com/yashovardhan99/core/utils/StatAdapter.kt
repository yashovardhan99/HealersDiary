package com.yashovardhan99.core.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.core.R
import com.yashovardhan99.core.databinding.LayoutStatBoxBinding

class StatAdapter(
    private val onClick: (Stat) -> Unit = {}
) : ListAdapter<Stat, StatAdapter.StatViewHolder>(StatDiffUtil()) {
    class StatViewHolder(val binding: LayoutStatBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(stat: Stat, onClick: (Stat) -> Unit) {
            binding.stat = stat
            binding.root.setOnClickListener { onClick(stat) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        return StatViewHolder(
            LayoutStatBoxBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.layout_stat_box
    }
}

class StatDiffUtil : DiffUtil.ItemCallback<Stat>() {
    override fun areItemsTheSame(oldItem: Stat, newItem: Stat): Boolean {
        return oldItem.description == newItem.description
    }

    override fun areContentsTheSame(oldItem: Stat, newItem: Stat): Boolean {
        return oldItem == newItem
    }
}
