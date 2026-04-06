package com.example.zorvyn.util

interface FileExporter {
    suspend fun saveAndShare(fileName: String, content: String)
}
