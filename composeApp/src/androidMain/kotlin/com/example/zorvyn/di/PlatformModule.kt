package com.example.zorvyn.di

import com.example.zorvyn.util.AndroidTextRecognizer
import com.example.zorvyn.util.TextRecognizer
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.zorvyn.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import com.example.zorvyn.util.AndroidFileExporter
import com.example.zorvyn.util.FileExporter
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<TextRecognizer> { AndroidTextRecognizer() }
    single<FileExporter> { AndroidFileExporter(androidContext()) }
    single<SqlDriver> { AndroidSqliteDriver(AppDatabase.Schema, androidContext(), "zorvyn_v2.db") }
}
