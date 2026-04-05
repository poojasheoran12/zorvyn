package org.example.project.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class TransactionType {
    INCOME, EXPENSE
}

enum class TransactionCategory(val icon: String) {
    FOOD("🍔"),
    TRAVEL("🚗"),
    SHOPPING("🛍️"),
    OTHERS("💰")
}

data class Transaction(
    val id: String,
    val amount: Double,
    val note: String,
    val type: TransactionType,
    val category: TransactionCategory,
    val timestamp: Instant = Clock.System.now()
) {
    val formattedDate: String
        get() = timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).run {
            "${dayOfMonth} ${month.name.take(3)}, ${dayOfWeek.name.take(3)}"
        }
}
