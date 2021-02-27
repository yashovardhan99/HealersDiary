package com.yashovardhan99.healersdiary.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.dashboard.ActivityAdapter.ActivityParentViewHolder
import com.yashovardhan99.healersdiary.databinding.ActivityCardBinding
import com.yashovardhan99.healersdiary.databinding.ActivitySeparatorBinding
import com.yashovardhan99.core.utils.ActivityParent

private const val VIEW_TYPE_ACTIVITY = 0
private const val VIEW_TYPE_SEPARATOR = 1

/**
 * Adapter for Activities (healings and payments)
 * @param onClick The onClick event when a view is clicked
 * @see ActivityParentViewHolder
 * @see ActivityParent
 * @see ActivityDiffUtils
 */
class ActivityAdapter(private val onClick: (ActivityParent, View) -> Unit) : ListAdapter<ActivityParent, ActivityAdapter.ActivityParentViewHolder>(ActivityDiffUtils()) {
    /**
     * The viewholders used for activity and separators
     * @param view The inflated view
     */
    sealed class ActivityParentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        /**
         * bind the viewholder with click listener and data
         */
        abstract fun bind(activity: ActivityParent, onClick: (ActivityParent, View) -> Unit)

        /**
         * View holder for an activity
         * @param binding The activity card binding inflated
         */
        class ActivityViewHolder(val binding: ActivityCardBinding) : ActivityParentViewHolder(binding.root) {
            override fun bind(activity: ActivityParent, onClick: (ActivityParent, View) -> Unit) {
                if (activity !is ActivityParent.Activity) throw IllegalArgumentException()
                binding.activity = activity
                binding.root.setOnClickListener { onClick(activity, binding.root) }
                binding.root.transitionName = "activity_trans_pos_${activity.id}_${activity.type.description}"
            }
        }

        /**
         * View holder for holding separators (headings)
         * @param binding an inflated ActivitySeparatorBinding
         */
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

/**
 * Comparison diff utils for ActivityParent items
 */
class ActivityDiffUtils : DiffUtil.ItemCallback<ActivityParent>() {
    override fun areItemsTheSame(oldItem: ActivityParent, newItem: ActivityParent): Boolean {
        return when {
            oldItem is ActivityParent.Activity && newItem is ActivityParent.Activity ->
                oldItem.id == newItem.id
            oldItem is ActivityParent.ActivitySeparator && newItem is ActivityParent.ActivitySeparator ->
                oldItem.heading == newItem.heading
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: ActivityParent, newItem: ActivityParent): Boolean {
        return oldItem == newItem
    }
}
