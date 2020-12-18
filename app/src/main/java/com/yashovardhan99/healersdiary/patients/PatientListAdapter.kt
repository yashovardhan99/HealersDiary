package com.yashovardhan99.healersdiary.patients

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.database.Patient
import com.yashovardhan99.healersdiary.databinding.PatientListItemBinding

class PatientListAdapter : ListAdapter<Patient, PatientListAdapter.PatientViewHolder>(PatientDiff()) {
    class PatientViewHolder(val binding: PatientListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(patient: Patient) {
            binding.patient = patient
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PatientViewHolder(PatientListItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(getItem(position))
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