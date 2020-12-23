package com.yashovardhan99.healersdiary.patients

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.database.Healing
import com.yashovardhan99.healersdiary.databinding.ItemCardHealingBinding


class HealingListAdapter(val onRequestDelete: (Healing) -> Unit) : PagingDataAdapter<Healing, HealingListAdapter.HealingViewHolder>(HealingDiffUtils()) {
    class HealingViewHolder(val binding: ItemCardHealingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(healing: Healing?, onRequestDelete: (Healing) -> Unit) {
            binding.healing = healing
            binding.root.setOnCreateContextMenuListener { menu, _, _ ->
                menu.add(0, 0, 0, R.string.delete).setOnMenuItemClickListener { _ ->
                    healing?.let { onRequestDelete(it); true } ?: false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealingViewHolder {
        return HealingViewHolder(ItemCardHealingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HealingViewHolder, position: Int) {
        holder.bind(getItem(position), onRequestDelete)
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