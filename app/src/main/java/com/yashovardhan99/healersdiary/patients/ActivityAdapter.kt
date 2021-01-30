package com.yashovardhan99.healersdiary.patients

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.databinding.ActivityInnerCardBinding
import com.yashovardhan99.healersdiary.utils.ActivityParent

class ActivityAdapter(private val onClick: (ActivityParent.Activity) -> Unit = {}) : ListAdapter<ActivityParent.Activity, ActivityAdapter.ActivityViewHolder>(ActivityDiffUtils()) {
    class ActivityViewHolder(val binding: ActivityInnerCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: ActivityParent.Activity, onClick: (ActivityParent.Activity) -> Unit = { }) {
            binding.activity = activity
            binding.root.setOnClickListener { onClick(activity) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        return ActivityViewHolder(ActivityInnerCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }
}

class ActivityDiffUtils : DiffUtil.ItemCallback<ActivityParent.Activity>() {
    override fun areItemsTheSame(oldItem: ActivityParent.Activity, newItem: ActivityParent.Activity): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ActivityParent.Activity, newItem: ActivityParent.Activity): Boolean {
        return oldItem == newItem
    }
}
