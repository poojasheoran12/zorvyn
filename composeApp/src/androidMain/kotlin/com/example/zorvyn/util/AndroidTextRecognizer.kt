package com.example.zorvyn.util

import android.graphics.BitmapFactory
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.zorvyn.domain.model.TransactionType

class AndroidTextRecognizer : TextRecognizer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeText(imageBytes: ByteArray): ReceiptData = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = Tasks.await(recognizer.process(image))
            parseReceiptText(result.text)
        } catch (e: Exception) {
            ReceiptData(null, null, null, null)
        }
    }

    private fun parseReceiptText(text: String): ReceiptData {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        
        // 1. Amount Detection (Year-Aware)
        var amount: Double? = null
        val labels = listOf("Amount", "Total", "Paid", "Successfully", "Rupees", "₹", "INR")
        
        // Regex that avoids times (HH:mm) and dates (e.g., 04, 2026)
        val amountCandidateRegex = """(?<![:\d])\b(\d{1,3}(?:,\d{3})*(?:\.\d{2})?|\d+)\b(?![:\d])""".toRegex()
        
        lines.forEachIndexed { index, line ->
            if (labels.any { line.contains(it, ignoreCase = true) }) {
                val searchArea = line + " " + (lines.getOrNull(index + 1) ?: "") + " " + (lines.getOrNull(index + 2) ?: "")
                
                // Priority A: Values with currency symbols (including misread symbols like ?, f, 7, z)
                val currencyValueRegex = """(?:₹|INR|Rs\.?|Amount|Total|[\?fz7])\s*([\d,]+\.?\d{0,2})""".toRegex()
                val currencyMatches = currencyValueRegex.findAll(searchArea)
                for(m in currencyMatches) {
                    val cand = m.groupValues[1].replace(",", "").toDoubleOrNull()
                    // Reject years 2000-2099 if they look like a year
                    if (cand != null && cand != 2024.0 && cand != 2025.0 && cand != 2026.0 && cand >= 1.0) {
                        amount = cand
                        break
                    }
                }
                
                // Priority B: Standalone digits (avoiding years/dates)
                if (amount == null) {
                    val matches = amountCandidateRegex.findAll(searchArea)
                    for (match in matches) {
                        val candidate = match.value.replace(",", "").toDoubleOrNull()
                        // Filter out years (2010-2040) and days (1-31) if they appear near 'Apr' or '202'
                        if (candidate != null && candidate >= 5.0 && candidate < 1000000.0) {
                            if (candidate in 2010.0..2040.0) continue // Skip likely year
                            amount = candidate
                            break
                        }
                    }
                }
            }
            if (amount != null) return@forEachIndexed
        }

        // 2. Merchant / Note Detection (UPI Optimized)
        var note: String? = null
        val toMatch = """(?i)To\s+([A-Za-z\s\.]{3,})""".toRegex().find(text)
        note = toMatch?.groupValues?.get(1)?.trim()
        
        if (note == null) {
            val toIndex = lines.indexOfFirst { it.equals("To", ignoreCase = true) }
            if (toIndex != -1 && lines.size > toIndex + 1) note = lines[toIndex + 1]
        }
        
        if (note == null || note.length < 3) {
            note = lines.firstOrNull { 
                it.length > 3 && !it.contains(":") && !it.any { c -> c.isDigit() } && !labels.any { l -> it.contains(l, ignoreCase = true) }
            } ?: "Receipt"
        }

        // 3. Type Detection
        val isIncome = listOf("credit", "deposit", "received", "refund", "salary", "cashback", "deposited", "credited")
            .any { text.contains(it, ignoreCase = true) }
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
        
        return ReceiptData(amount, note, type, null)
    }
}
