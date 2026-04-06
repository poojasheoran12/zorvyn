package com.example.zorvyn.util

import com.example.zorvyn.domain.model.TransactionType

data class ReceiptData(
    val amount: Double? = null,
    val note: String? = null,
    val type: TransactionType? = null,
    val date: String? = null
)

interface TextRecognizer {
    suspend fun recognizeText(imageBytes: ByteArray): ReceiptData
}
