package com.example.zorvyn.di

import com.example.zorvyn.util.AndroidTextRecognizer
import com.example.zorvyn.util.TextRecognizer
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.zorvyn.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<TextRecognizer> { AndroidTextRecognizer() }
    single<SqlDriver> { AndroidSqliteDriver(AppDatabase.Schema, androidContext(), "app.db") }
}
