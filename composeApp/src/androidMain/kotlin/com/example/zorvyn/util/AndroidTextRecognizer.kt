package com.example.zorvyn.util

import android.graphics.BitmapFactory
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidTextRecognizer : TextRecognizer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeText(imageBytes: ByteArray): ReceiptData = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = Tasks.await(recognizer.process(image))
            parseReceiptText(result.text)
        } catch (e: Exception) {
            ReceiptData(null, null, null)
        }
    }

    private fun parseReceiptText(text: String): ReceiptData {
        // Simple regex to find amounts like "₹ 1,234" or "Total: ₹ 1,234"
        val amountRegex = """(?i)(₹|INR|Total|Paid)\s?([\d,]+(\.\d{2})?)""".toRegex()
        val match = amountRegex.find(text)
        val amountStr = match?.groupValues?.get(2)?.replace(",", "")
        val amount = amountStr?.toDoubleOrNull()
        
        // Extract first line as note/merchant
        val note = text.split("\n").firstOrNull() { it.length > 3 }?.trim() ?: "Receipt"
        
        return ReceiptData(amount, note, null)
    }
}
