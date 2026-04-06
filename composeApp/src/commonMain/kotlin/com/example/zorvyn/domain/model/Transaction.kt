package com.example.zorvyn.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    INCOME, EXPENSE
}

@Serializable
enum class TransactionCategory(val icon: String) {
    FOOD("🍔"),
    TRANSPORT("🚗"),
    TRAVEL("✈️"),
    SHOPPING("🛍️"),
    HEALTHCARE("🏥"),
    RENT("🏠"),
    SALARY("💰"),
    OTHER("✨")
}

@Serializable
data class Transaction(
    val id: String,
    val amount: Double,
    val note: String,
    val type: TransactionType,
    val category: TransactionCategory,
    val timestamp: Instant = Clock.System.now(),
    val receiptPath: String? = null
) {
    val formattedDate: String
        get() = timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).run {
            "${dayOfMonth} ${month.name.take(3)}, ${dayOfWeek.name.take(3)}"
        }
}
