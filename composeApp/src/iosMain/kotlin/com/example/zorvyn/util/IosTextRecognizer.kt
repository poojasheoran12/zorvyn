package com.example.zorvyn.util

import platform.UIKit.UIImage
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRecognizedText
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.imageWithData
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

import com.example.zorvyn.domain.model.TransactionType

class IosTextRecognizer : TextRecognizer {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun recognizeText(imageBytes: ByteArray): ReceiptData = suspendCoroutine { continuation ->
        val nsData = imageBytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = imageBytes.size.toULong())
        }
        val uiImage = UIImage.imageWithData(nsData) ?: return@suspendCoroutine continuation.resume(ReceiptData(null, null, null, null))

        val request = VNRecognizeTextRequest { request, error ->
            if (error != null) {
                continuation.resume(ReceiptData(null, null, null, null))
                return@VNRecognizeTextRequest
            }
            val observations = request?.results as? List<VNRecognizedTextObservation>
            val text = observations?.mapNotNull { it.topCandidates(1u).firstOrNull() as? VNRecognizedText }?.joinToString("\n") { it.string }

            continuation.resume(parseReceiptText(text ?: ""))
        }

        val handler = VNImageRequestHandler(cgImage = uiImage.CGImage, options = emptyMap<Any?, Any?>())
        handler.performRequests(listOf(request) as List<*>, null)
    }

    private fun parseReceiptText(text: String): ReceiptData {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        val currencyRegex = """(₹|INR|Rs\.?)\s?([\d,]+(\.\d{2})?)""".toRegex()
        var amount: Double? = currencyRegex.find(text)?.groupValues?.get(2)?.replace(",", "")?.toDoubleOrNull()

        if (amount == null) {
            val labelRegex = """(?i)(Total|Amount|Paid|Paid Successfully)\s*:?\s*""".toRegex()
            lines.forEachIndexed { index, line ->
                if (labelRegex.containsMatchIn(line)) {
                    val nextLine = lines.getOrNull(index + 1) ?: ""
                    val valueRegex = """([\d,]+(\.\d{2})?)""".toRegex()
                    amount = valueRegex.find(line.replace(labelRegex, ""))?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
                        ?: valueRegex.find(nextLine)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
                    if (amount != null) return@forEachIndexed
                }
            }
        }

        val toMatch = """(?i)To\s+([A-Z\s]{3,})""".toRegex().find(text)
        var note = toMatch?.groupValues?.get(1)?.trim()

        if (note == null) {
            note = lines.firstOrNull { 
                it.length > 3 && !it.contains(":") && !it.any { char -> char.isDigit() } 
            } ?: "Receipt"
        }

        val lowerText = text.lowercase()
        val incomeKeywords = listOf("credit", "deposit", "received", "refund", "salary", "cashback", "deposited")
        val expenseKeywords = listOf("paid", "transfer", "sent", "debit", "subscription", "bill")

        val isIncome = incomeKeywords.any { lowerText.contains(it) }
        val isExpense = expenseKeywords.any { lowerText.contains(it) }

        val type = when {
            isIncome -> TransactionType.INCOME
            isExpense -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE 
        }

        return ReceiptData(amount, note, type, null)
    }
}
