package com.yashovardhan99.healersdiary.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.databinding.EmptyDashboardBinding

class EmptyStateAdapter(var isVisible: Boolean) : RecyclerView.Adapter<EmptyStateAdapter.EmptyStateViewHolder>() {
    class EmptyStateViewHolder(val binding: EmptyDashboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.root.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmptyStateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EmptyStateViewHolder(EmptyDashboardBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: EmptyStateViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return if (isVisible) 1 else 0
    }
}