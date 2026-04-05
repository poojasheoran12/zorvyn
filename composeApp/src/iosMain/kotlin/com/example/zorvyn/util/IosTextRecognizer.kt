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

class IosTextRecognizer : TextRecognizer {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun recognizeText(imageBytes: ByteArray): ReceiptData = suspendCoroutine { continuation ->
        val nsData = imageBytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = imageBytes.size.toULong())
        }
        val uiImage = UIImage.imageWithData(nsData) ?: return@suspendCoroutine continuation.resume(ReceiptData(null, null, null))
        
        val request = VNRecognizeTextRequest { request, error ->
            if (error != null) {
                continuation.resume(ReceiptData(null, null, null))
                return@VNRecognizeTextRequest
            }
            val observations = request?.results as? List<VNRecognizedTextObservation>
            val text = observations?.mapNotNull { it.topCandidates(1u).firstOrNull() as? VNRecognizedText }?.joinToString("\n") { it.string }
            
            continuation.resume(parseReceiptText(text ?: ""))
        }
        
        val handler = VNImageRequestHandler(cgImage = uiImage.CGImage, options = emptyMap<Any?, Any?>())
        handler.performRequests(listOf(request), null)
    }

    private fun parseReceiptText(text: String): ReceiptData {
        val amountRegex = """(?i)(₹|INR|Total|Paid)\s?([\d,]+(\.\d{2})?)""".toRegex()
        val match = amountRegex.find(text)
        val amountStr = match?.groupValues?.get(2)?.replace(",", "")
        val amount = amountStr?.toDoubleOrNull()
        val note = text.split("\n").firstOrNull() { it.length > 3 }?.trim() ?: "Receipt"
        return ReceiptData(amount, note, null)
    }
}
