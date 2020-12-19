package com.yashovardhan99.healersdiary.create

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.databinding.ItemChoosePatientBinding
import com.yashovardhan99.healersdiary.patients.PatientDiff
import timber.log.Timber

class ChoosePatientAdapter(private val onSelect: (Patient) -> Unit) : ListAdapter<Patient, ChoosePatientAdapter.ChoosePatientViewHolder>(PatientDiff()) {
    class ChoosePatientViewHolder(val binding: ItemChoosePatientBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(patient: Patient, onSelect: (Patient) -> Unit) {
            binding.patient = patient
            binding.root.setOnClickListener {
                onSelect(patient)
                Timber.d("Selected patient = $patient")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePatientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ChoosePatientViewHolder(ItemChoosePatientBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ChoosePatientViewHolder, position: Int) {
        holder.bind(getItem(position), onSelect)
    }
}