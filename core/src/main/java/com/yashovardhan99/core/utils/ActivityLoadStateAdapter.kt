package com.yashovardhan99.core.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.core.R
import com.yashovardhan99.core.databinding.LayoutActivityFooterBinding

class ActivityLoadStateAdapter : LoadStateAdapter<ActivityLoadStateAdapter.LoadStateViewHolder>() {
    abstract class LoadStateViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        class ProgressViewHolder(val binding: LayoutActivityFooterBinding) :
            LoadStateViewHolder(binding.root) {
            fun bind(loadState: LoadState) {
                if (loadState is LoadState.Loading) {
                    binding.root.animate().apply {
                        translationY(0f)
                        alpha(1f)
                        duration = 200
                    }.start()
                    // binding.progressCircular.show()
                } else {
                    // binding.progressCircular.setVisibilityAfterHide(View.GONE)
                    binding.root.animate().apply {
                        translationY(64f)
                        alpha(0f)
                        duration = 200
                    }.start()
                    // binding.progressCircular.hide()
                }
            }
        }
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        if (holder is LoadStateViewHolder.ProgressViewHolder) holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return LoadStateViewHolder.ProgressViewHolder(
            LayoutActivityFooterBinding.inflate(inflater, parent, false)
        )
    }

    override fun getStateViewType(loadState: LoadState): Int {
        return R.layout.layout_activity_footer
    }
}
