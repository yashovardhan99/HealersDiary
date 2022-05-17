package com.yashovardhan99.healersdiary.utils

import com.yashovardhan99.healersdiary.dashboard.DashboardRepository
import com.yashovardhan99.healersdiary.dashboard.DashboardRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {
    /**
     * Instantiates [DashboardRepositoryImpl] and injects it as [DashboardRepository] into different ViewModels as needed
     * */
    @Binds
    abstract fun bindDashboardRepository(dashboardRepositoryImpl: DashboardRepositoryImpl): DashboardRepository
}