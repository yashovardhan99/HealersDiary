package com.yashovardhan99.healersdiary.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.core.utils.ActivityParent
import com.yashovardhan99.healersdiary.dashboard.ActivityAdapter.ActivityParentViewHolder
import com.yashovardhan99.healersdiary.databinding.ActivityCardBinding
import com.yashovardhan99.healersdiary.databinding.ActivitySeparatorBinding

private const val VIEW_TYPE_ACTIVITY = 0
private const val VIEW_TYPE_SEPARATOR = 1
private const val VIEW_TYPE_LOADING = 2

/**
 * Adapter for Activities (healings and payments)
 * @param onClick The onClick event when a view is clicked
 * @see ActivityParentViewHolder
 * @see ActivityParent
 * @see ActivityDiffUtils
 */
class ActivityAdapter(private val onClick: (ActivityParent, View) -> Unit) :
    PagingDataAdapter<ActivityParent, ActivityParentViewHolder>(ActivityDiffUtils()) {
    /**
     * The viewholders used for activity and separators
     * @param view The inflated view
     */
    sealed class ActivityParentViewHolder(
        val viewLifecycleOwner: LifecycleOwner?,
        val view: View
    ) :
        RecyclerView.ViewHolder(view) {

        /**
         * bind the viewholder with click listener and data
         */
        abstract fun bind(activity: ActivityParent?, onClick: (ActivityParent, View) -> Unit)

        /**
         * View holder for an activity
         * @param binding The activity card binding inflated
         */
        class ActivityViewHolder(
            lifecycleOwner: LifecycleOwner?,
            val binding: ActivityCardBinding
        ) :
            ActivityParentViewHolder(lifecycleOwner, binding.root) {

            init {
                binding.skeleton.nameSkeleton.isVisible = false
                binding.skeleton.iconSkeleton.isVisible = false
                binding.skeleton.amountSkeleton.isVisible = false
                binding.skeleton.typeSkeleton.isVisible = false
            }

            override fun bind(activity: ActivityParent?, onClick: (ActivityParent, View) -> Unit) {
                if (activity != null) {
                    if (activity !is ActivityParent.Activity) throw IllegalArgumentException()
                    binding.activity = activity
                    binding.root.setOnClickListener { onClick(activity, binding.root) }
                    binding.root.transitionName =
                        "activity_trans_pos_${activity.id}_${activity.type.description}"
                    binding.skeleton.nameSkeleton.startHideAnimation()
                    binding.skeleton.amountSkeleton.startHideAnimation()
                    binding.skeleton.typeSkeleton.startHideAnimation()
                    binding.skeleton.iconSkeleton.startHideAnimation()
                } else {
                    binding.root.setOnClickListener(null)
                    binding.activity = null
                    binding.skeleton.nameSkeleton.isVisible = true
                    binding.skeleton.amountSkeleton.isVisible = true
                    binding.skeleton.iconSkeleton.isVisible = true
                    binding.skeleton.typeSkeleton.isVisible = true
                }
            }

            private fun View.startHideAnimation() {
                if (!isVisible) return
                animate().apply { duration = 200 }
                    .alpha(0f)
                    .withEndAction { visibility = View.GONE }
                    .start()
            }
        }

        /**
         * View holder for holding separators (headings)
         * @param binding an inflated ActivitySeparatorBinding
         */
        class SeparatorViewHolder(
            viewTreeLifecycleOwner: LifecycleOwner?,
            val binding: ActivitySeparatorBinding
        ) :
            ActivityParentViewHolder(viewTreeLifecycleOwner, binding.root) {
            override fun bind(activity: ActivityParent?, onClick: (ActivityParent, View) -> Unit) {
                if (activity !is ActivityParent.ActivitySeparator) throw IllegalArgumentException()
                binding.heading = activity.heading
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActivityParentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ACTIVITY -> ActivityParentViewHolder.ActivityViewHolder(
                parent.findViewTreeLifecycleOwner(),
                ActivityCardBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_SEPARATOR -> ActivityParentViewHolder.SeparatorViewHolder(
                parent.findViewTreeLifecycleOwner(),
                ActivitySeparatorBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_LOADING -> ActivityParentViewHolder.ActivityViewHolder(
                parent.findViewTreeLifecycleOwner(),
                ActivityCardBinding.inflate(inflater, parent, false)
            )
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
            null -> VIEW_TYPE_LOADING
            else -> throw IllegalArgumentException()
        }
    }
}

/**
 * Comparison diff utils for ActivityParent items
 */
class ActivityDiffUtils : DiffUtil.ItemCallback<ActivityParent>() {
    override fun areItemsTheSame(
        oldItem: ActivityParent,
        newItem: ActivityParent
    ): Boolean {
        return when {
            oldItem is ActivityParent.Activity && newItem is ActivityParent.Activity ->
                oldItem.id == newItem.id
            oldItem is ActivityParent.ActivitySeparator &&
                newItem is ActivityParent.ActivitySeparator -> oldItem.heading == newItem.heading
            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: ActivityParent,
        newItem: ActivityParent
    ): Boolean {
        return oldItem == newItem
    }
}
