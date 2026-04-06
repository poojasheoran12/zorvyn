package com.example.zorvyn.di

import com.example.zorvyn.data.repository.*
import com.example.zorvyn.domain.repository.*
import com.example.zorvyn.presentation.*
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import com.example.zorvyn.database.AppDatabase

val appModule = module {
    includes(platformModule)
    single { Firebase.firestore }
    single { AppDatabase(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get()) }
    single<GoalRepository> { GoalRepositoryImpl(get(), get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl() }
    viewModelOf(::DashboardViewModel)
    viewModelOf(::TransactionViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::GoalViewModel)
    viewModelOf(::BudgetViewModel)
}
