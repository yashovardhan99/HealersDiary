package com.yashovardhan99.healersdiary.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.core.utils.ActivityParent
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.dashboard.ActivityAdapter.ActivityParentViewHolder
import com.yashovardhan99.healersdiary.databinding.ActivityCardBinding
import com.yashovardhan99.healersdiary.databinding.ActivityInnerCardBinding
import com.yashovardhan99.healersdiary.databinding.ActivitySeparatorBinding

private const val VIEW_TYPE_ACTIVITY = R.layout.activity_card
private const val VIEW_TYPE_SEPARATOR = R.layout.activity_separator
private const val VIEW_TYPE_INNER_ACTIVITY = R.layout.activity_inner_card

/**
 * Adapter for Activities (healings and payments)
 * @param onClick The onClick event when a view is clicked
 * @param useInnerCardForActivity If the activity is inflated from a patient detail view and
 * should use inner activity card
 * @see ActivityParentViewHolder
 * @see ActivityParent
 * @see ActivityDiffUtils
 */
class ActivityAdapter(
    private val useInnerCardForActivity: Boolean,
    private val onClick: (ActivityParent, View) -> Unit,
    private val onRequestEdit: (ActivityParent.Activity) -> Unit = {},
    private val onRequestDelete: (ActivityParent.Activity) -> Unit = {}
) :
    PagingDataAdapter<ActivityParent, ActivityParentViewHolder>(ActivityDiffUtils()) {
    /**
     * The viewholders used for activity and separators
     * @param view The inflated view
     */
    sealed class ActivityParentViewHolder(
        val view: View
    ) : RecyclerView.ViewHolder(view) {

        /**
         * bind the viewholder with click listener and data
         */
        abstract fun bind(
            activity: ActivityParent,
            onRequestEdit: (ActivityParent.Activity) -> Unit,
            onRequestDelete: (ActivityParent.Activity) -> Unit
        )

        /**
         * View holder for an activity
         * @param binding The activity card binding inflated
         */
        class ActivityViewHolder(
            onClick: (Int, View) -> Unit,
            val binding: ActivityCardBinding
        ) : ActivityParentViewHolder(binding.root) {

            init {
                binding.root.setOnClickListener { onClick(bindingAdapterPosition, binding.root) }
            }

            override fun bind(
                activity: ActivityParent,
                onRequestEdit: (ActivityParent.Activity) -> Unit,
                onRequestDelete: (ActivityParent.Activity) -> Unit,
            ) {
                if (activity !is ActivityParent.Activity) throw IllegalArgumentException()
                binding.activity = activity
                binding.root.transitionName =
                    "activity_trans_pos_${activity.id}_${activity.type.description}"
                if (activity.type !is ActivityParent.Activity.Type.PATIENT) {
                    binding.root.setOnCreateContextMenuListener { menu, _, _ ->
                        menu.add(R.string.edit).setOnMenuItemClickListener { _ ->
                            onRequestEdit(activity)
                            true
                        }
                        menu.add(R.string.delete).setOnMenuItemClickListener { _ ->
                            onRequestDelete(activity)
                            true
                        }
                    }
                } else {
                    binding.root.setOnCreateContextMenuListener(null)
                }
            }
        }

        /**
         * View holder for holding separators (headings)
         * @param binding an inflated ActivitySeparatorBinding
         */
        class SeparatorViewHolder(
            val binding: ActivitySeparatorBinding
        ) : ActivityParentViewHolder(binding.root) {
            override fun bind(
                activity: ActivityParent,
                onRequestEdit: (ActivityParent.Activity) -> Unit,
                onRequestDelete: (ActivityParent.Activity) -> Unit,
            ) {
                if (activity !is ActivityParent.ActivitySeparator) throw IllegalArgumentException()
                binding.heading = activity.heading
            }
        }

        /**
         * View holder for an activity inside patient detail page
         * @param binding The activity card binding inflated
         */
        class InnerActivityViewHolder(
            onClick: (Int, View) -> Unit,
            val binding: ActivityInnerCardBinding
        ) : ActivityParentViewHolder(binding.root) {

            init {
                binding.root.setOnClickListener { onClick(bindingAdapterPosition, binding.root) }
            }

            override fun bind(
                activity: ActivityParent,
                onRequestEdit: (ActivityParent.Activity) -> Unit,
                onRequestDelete: (ActivityParent.Activity) -> Unit,
            ) {
                if (activity !is ActivityParent.Activity) throw IllegalArgumentException()
                binding.activity = activity
                binding.root.transitionName =
                    "activity_trans_pos_${activity.id}_${activity.type.description}"
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
                ::onClick, ActivityCardBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_SEPARATOR -> ActivityParentViewHolder.SeparatorViewHolder(
                ActivitySeparatorBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_INNER_ACTIVITY -> ActivityParentViewHolder.InnerActivityViewHolder(
                ::onClick, ActivityInnerCardBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException()
        }
    }

    private fun onClick(position: Int, view: View) {
        val item = getItem(position)
        if (item is ActivityParent.Activity) onClick(item, view)
    }

    override fun onBindViewHolder(holder: ActivityParentViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, onRequestEdit, onRequestDelete) }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ActivityParent.Activity -> {
                if (useInnerCardForActivity) VIEW_TYPE_INNER_ACTIVITY
                else VIEW_TYPE_ACTIVITY
            }
            is ActivityParent.ActivitySeparator -> VIEW_TYPE_SEPARATOR
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
                oldItem.id == newItem.id && oldItem.type == newItem.type
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
