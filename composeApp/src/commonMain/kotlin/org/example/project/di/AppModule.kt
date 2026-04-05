package org.example.project.di

import org.example.project.data.repository.FinancialRepositoryImpl
import org.example.project.domain.repository.FinancialRepository
import org.example.project.presentation.DashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<FinancialRepository> { FinancialRepositoryImpl() }
    viewModelOf(::DashboardViewModel)
}
