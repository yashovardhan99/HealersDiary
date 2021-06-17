package com.yashovardhan99.core.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.core.R
import com.yashovardhan99.core.databinding.EmptyDashboardBinding

class EmptyStateAdapter :
    ListAdapter<EmptyState, EmptyStateAdapter.EmptyStateViewHolder>(EmptyStateDiffUtil()) {
    class EmptyStateViewHolder(val binding: EmptyDashboardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(emptyState: EmptyState) {
            binding.emptyState = emptyState
            binding.noDataImage.setImageDrawable(
                ResourcesCompat.getDrawable(
                    binding.root.resources,
                    emptyState.drawable,
                    binding.root.context.theme
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmptyStateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EmptyStateViewHolder(EmptyDashboardBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: EmptyStateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.empty_dashboard
    }
}

class EmptyStateDiffUtil : DiffUtil.ItemCallback<EmptyState>() {
    override fun areItemsTheSame(oldItem: EmptyState, newItem: EmptyState): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: EmptyState, newItem: EmptyState): Boolean {
        return oldItem == newItem
    }
}

enum class EmptyState(
    @DrawableRes val drawable: Int,
    @StringRes val headerText: Int,
    @StringRes val subText: Int
) {
    DASHBOARD(
        R.drawable.no_data,
        R.string.not_much_to_show_here,
        R.string.add_some_healings_to_get_started
    ),
    PATIENTS(
        R.drawable.add_user_illustration,
        R.string.a_bit_lonely,
        R.string.click_to_add_patient
    ),
    ANALYTICS(R.drawable.analytics_illustration, R.string.nothing_here_yet, R.string.we_are_working)
}
