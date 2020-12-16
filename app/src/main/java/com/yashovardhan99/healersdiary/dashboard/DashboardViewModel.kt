package com.yashovardhan99.healersdiary.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel

class DashboardViewModel @ViewModelInject constructor(private val repository: DashboardRepository) : ViewModel() {
    val patients = repository.patients
}