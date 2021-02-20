package com.yashovardhan99.healersdiary.patients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.ActivitySeparatorBinding
import com.yashovardhan99.healersdiary.databinding.ItemCardHealingBinding
import com.yashovardhan99.healersdiary.utils.HealingParent

private const val VIEW_TYPE_HEALING = R.layout.item_card_healing
private const val VIEW_TYPE_SEPARATOR = R.layout.activity_separator

class HealingListAdapter(private val onRequestEdit: (HealingParent.Healing) -> Unit, private val onRequestDelete: (HealingParent.Healing) -> Unit) : PagingDataAdapter<HealingParent, HealingListAdapter.HealingParentViewHolder>(HealingDiffUtils()) {
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HealingParent.Healing -> VIEW_TYPE_HEALING
            is HealingParent.HealingSeparator -> VIEW_TYPE_SEPARATOR
            null -> VIEW_TYPE_HEALING
        }
    }

    sealed class HealingParentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(healing: HealingParent?, onRequestEdit: (HealingParent.Healing) -> Unit, onRequestDelete: (HealingParent.Healing) -> Unit)
        class HealingViewHolder(val binding: ItemCardHealingBinding) : HealingParentViewHolder(binding.root) {
            override fun bind(healing: HealingParent?, onRequestEdit: (HealingParent.Healing) -> Unit, onRequestDelete: (HealingParent.Healing) -> Unit) {
                if (healing is HealingParent.Healing) {
                    binding.healing = healing
                    binding.root.setOnCreateContextMenuListener { menu, _, _ ->
                        menu.add(R.string.edit).setOnMenuItemClickListener { _ ->
                            onRequestEdit(healing)
                            true
                        }
                        menu.add(R.string.delete).setOnMenuItemClickListener { _ ->
                            onRequestDelete(healing)
                            true
                        }
                    }
                }
            }
        }

        class SeparatorViewHolder(val binding: ActivitySeparatorBinding) : HealingParentViewHolder(binding.root) {
            override fun bind(healing: HealingParent?, onRequestEdit: (HealingParent.Healing) -> Unit, onRequestDelete: (HealingParent.Healing) -> Unit) {
                if (healing !is HealingParent.HealingSeparator) throw IllegalArgumentException()
                binding.heading = healing.heading

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealingParentViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEALING -> HealingParentViewHolder.HealingViewHolder(ItemCardHealingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_SEPARATOR -> HealingParentViewHolder.SeparatorViewHolder(ActivitySeparatorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: HealingParentViewHolder, position: Int) {
        holder.bind(getItem(position), onRequestEdit, onRequestDelete)
    }
}

class HealingDiffUtils : DiffUtil.ItemCallback<HealingParent>() {
    override fun areItemsTheSame(oldItem: HealingParent, newItem: HealingParent): Boolean {
        return if (oldItem is HealingParent.Healing && newItem is HealingParent.Healing) oldItem.id == newItem.id
        else if (oldItem is HealingParent.HealingSeparator && newItem is HealingParent.HealingSeparator) oldItem.heading == newItem.heading
        else false
    }

    override fun areContentsTheSame(oldItem: HealingParent, newItem: HealingParent): Boolean {
        return oldItem == newItem
    }

}