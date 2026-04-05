package com.example.zorvyn.di

import com.example.zorvyn.data.repository.FinancialRepositoryImpl
import com.example.zorvyn.domain.repository.FinancialRepository
import com.example.zorvyn.presentation.DashboardViewModel
import com.example.zorvyn.presentation.TransactionViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

import com.example.zorvyn.database.AppDatabase

expect val platformModule: Module

val appModule = module {
    includes(platformModule)
    single { Firebase.firestore }
    single { AppDatabase(get()) }
    single<FinancialRepository> { FinancialRepositoryImpl(get(), get()) }
    viewModelOf(::DashboardViewModel)
    viewModelOf(::TransactionViewModel)
}
