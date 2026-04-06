package com.example.zorvyn.di

import com.example.zorvyn.util.IosTextRecognizer
import com.example.zorvyn.util.TextRecognizer
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.zorvyn.database.AppDatabase
import com.example.zorvyn.util.IosFileExporter
import com.example.zorvyn.util.FileExporter
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<TextRecognizer> { IosTextRecognizer() }
    single<FileExporter> { IosFileExporter() }
    single<SqlDriver> { NativeSqliteDriver(AppDatabase.Schema, "zorvyn_v2.db") }
}
