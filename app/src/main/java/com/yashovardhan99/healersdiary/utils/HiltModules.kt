package com.yashovardhan99.healersdiary.utils

import com.yashovardhan99.healersdiary.create.CreateRepository
import com.yashovardhan99.healersdiary.create.CreateRepositoryImpl
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
     * Instantiates [CreateRepositoryImpl] and injects it as [CreateRepository] into different ViewModels as needed
     * */
    @Binds
    abstract fun bindCreateRepository(createRepositoryImpl: CreateRepositoryImpl): CreateRepository

    /**
     * Instantiates [DashboardRepositoryImpl] and injects it as [DashboardRepository] into different ViewModels as needed
     * */
    @Binds
    abstract fun bindDashboardRepository(dashboardRepositoryImpl: DashboardRepositoryImpl): DashboardRepository
}