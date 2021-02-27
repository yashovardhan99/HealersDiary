package com.yashovardhan99.healersdiary.patients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.ActivityDiffUtils
import com.yashovardhan99.healersdiary.databinding.ActivityInnerCardBinding
import com.yashovardhan99.healersdiary.databinding.ActivitySeparatorBinding
import com.yashovardhan99.core.utils.ActivityParent

private const val VIEW_TYPE_ACTIVITY = R.layout.activity_inner_card
private const val VIEW_TYPE_SEPARATOR = R.layout.activity_separator

class ActivityAdapter(private val onClick: (ActivityParent) -> Unit = {}) : ListAdapter<ActivityParent, ActivityAdapter.ActivityParentViewHolder>(ActivityDiffUtils()) {
    sealed class ActivityParentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(activity: ActivityParent, onClick: (ActivityParent) -> Unit)
        class ActivityViewHolder(val binding: ActivityInnerCardBinding) : ActivityParentViewHolder(binding.root) {
            override fun bind(activity: ActivityParent, onClick: (ActivityParent) -> Unit) {
                if (activity !is ActivityParent.Activity) throw IllegalArgumentException()
                binding.activity = activity
                binding.root.setOnClickListener { onClick(activity) }
            }
        }

        class SeparatorViewHolder(val binding: ActivitySeparatorBinding) : ActivityParentViewHolder(binding.root) {
            override fun bind(activity: ActivityParent, onClick: (ActivityParent) -> Unit) {
                if (activity !is ActivityParent.ActivitySeparator) throw IllegalArgumentException()
                binding.heading = activity.heading
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ActivityParent.Activity -> VIEW_TYPE_ACTIVITY
            is ActivityParent.ActivitySeparator -> VIEW_TYPE_SEPARATOR
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityParentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ACTIVITY -> ActivityParentViewHolder.ActivityViewHolder(ActivityInnerCardBinding.inflate(inflater, parent, false))
            VIEW_TYPE_SEPARATOR -> ActivityParentViewHolder.SeparatorViewHolder(ActivitySeparatorBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: ActivityParentViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }
}
