package com.yashovardhan99.healersdiary.online

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yashovardhan99.healersdiary.OnlineModuleDependencies
import com.yashovardhan99.healersdiary.online.databinding.FragmentImportFirebaseBinding
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

class ImportFirebaseFragment : Fragment() {
    @Inject
    lateinit var viewModel: ImportFirebaseViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        val coreModuleDependencies = EntryPointAccessors.fromApplication(
                requireActivity().applicationContext,
                OnlineModuleDependencies::class.java)
        DaggerOnlineComponent.builder()
                .context(requireContext())
                .appDependencies(coreModuleDependencies)
                .build().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentImportFirebaseBinding.inflate(inflater, container, false)
        binding.login.setOnClickListener {
            viewModel.importCompleted()
        }
        return binding.root
    }
}