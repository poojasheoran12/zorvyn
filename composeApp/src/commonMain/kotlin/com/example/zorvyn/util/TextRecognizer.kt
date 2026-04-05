package com.example.zorvyn.util

data class ReceiptData(
    val amount: Double? = null,
    val note: String? = null,
    val date: String? = null
)

interface TextRecognizer {
    suspend fun recognizeText(imageBytes: ByteArray): ReceiptData
}
