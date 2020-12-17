package com.yashovardhan99.healersdiary.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.FragmentHomeBinding
import com.yashovardhan99.healersdiary.utils.Header
import com.yashovardhan99.healersdiary.utils.StatAdapter
import com.yashovardhan99.healersdiary.utils.getIcon
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val viewModel: DashboardViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.header = context?.run {
            Header(getIcon(R.drawable.home),
                    resources.getString(R.string.app_name),
                    getIcon(R.drawable.settings))
        }
        val adapter = StatAdapter()
        binding.recycler.adapter = adapter
        adapter.submitList(viewModel.dashboardStats)
        val layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position <= 3) 1
                else 2
            }
        }
        binding.recycler.layoutManager = layoutManager
        return binding.root
    }
}