package com.yashovardhan99.healersdiary.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.databinding.ActivityCardBinding
import com.yashovardhan99.healersdiary.databinding.ActivitySeparatorBinding
import com.yashovardhan99.healersdiary.utils.ActivityParent

private const val VIEW_TYPE_ACTIVITY = 0
private const val VIEW_TYPE_SEPARATOR = 1

class ActivityAdapter(private val onClick: (ActivityParent, View) -> Unit) : ListAdapter<ActivityParent, ActivityAdapter.ActivityParentViewHolder>(ActivityDiffUtils()) {
    sealed class ActivityParentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(activity: ActivityParent, onClick: (ActivityParent, View) -> Unit)
        class ActivityViewHolder(val binding: ActivityCardBinding) : ActivityParentViewHolder(binding.root) {
            override fun bind(activity: ActivityParent, onClick: (ActivityParent, View) -> Unit) {
                if (activity !is ActivityParent.Activity) throw IllegalArgumentException()
                binding.activity = activity
                binding.root.setOnClickListener { onClick(activity, binding.root) }
                binding.root.transitionName = "activity_trans_pos_${activity.id}_${activity.type.description}"
            }
        }

        class SeparatorViewHolder(val binding: ActivitySeparatorBinding) : ActivityParentViewHolder(binding.root) {
            override fun bind(activity: ActivityParent, onClick: (ActivityParent, View) -> Unit) {
                if (activity !is ActivityParent.ActivitySeparator) throw IllegalArgumentException()
                binding.heading = activity.heading
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityParentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ACTIVITY -> ActivityParentViewHolder.ActivityViewHolder(ActivityCardBinding.inflate(inflater, parent, false))
            VIEW_TYPE_SEPARATOR -> ActivityParentViewHolder.SeparatorViewHolder(ActivitySeparatorBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: ActivityParentViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ActivityParent.Activity -> VIEW_TYPE_ACTIVITY
            is ActivityParent.ActivitySeparator -> VIEW_TYPE_SEPARATOR
        }
    }
}

class ActivityDiffUtils : DiffUtil.ItemCallback<ActivityParent>() {
    override fun areItemsTheSame(oldItem: ActivityParent, newItem: ActivityParent): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ActivityParent, newItem: ActivityParent): Boolean {
        return oldItem == newItem
    }
}
