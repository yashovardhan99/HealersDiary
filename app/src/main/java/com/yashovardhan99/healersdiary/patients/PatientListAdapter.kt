package com.yashovardhan99.healersdiary.patients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.healersdiary.databinding.PatientListItemBinding

class PatientListAdapter(private val onPatientSelected: (Patient, View) -> Unit) : ListAdapter<Patient, PatientListAdapter.PatientViewHolder>(PatientDiff()) {
    class PatientViewHolder(val binding: PatientListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(patient: Patient, onPatientSelected: (Patient, View) -> Unit) {
            binding.patient = patient
            binding.root.setOnClickListener { onPatientSelected(patient, binding.root) }
            binding.root.transitionName = "patient_trans_pos_${patient.id}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PatientViewHolder(PatientListItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(getItem(position), onPatientSelected)
    }
}

class PatientDiff : DiffUtil.ItemCallback<Patient>() {
    override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
        return oldItem == newItem
    }

}