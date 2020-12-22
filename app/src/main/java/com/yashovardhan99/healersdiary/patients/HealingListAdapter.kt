package com.yashovardhan99.healersdiary.patients

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.database.Healing
import com.yashovardhan99.healersdiary.databinding.ItemCardHealingBinding


class HealingListAdapter : PagingDataAdapter<Healing, HealingListAdapter.HealingViewHolder>(HealingDiffUtils()) {
    class HealingViewHolder(val binding: ItemCardHealingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(healing: Healing?) {
            binding.healing = healing
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealingViewHolder {
        return HealingViewHolder(ItemCardHealingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HealingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class HealingDiffUtils : DiffUtil.ItemCallback<Healing>() {
    override fun areItemsTheSame(oldItem: Healing, newItem: Healing): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Healing, newItem: Healing): Boolean {
        return oldItem == newItem
    }

}