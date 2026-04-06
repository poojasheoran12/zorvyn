package com.example.zorvyn.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: String,
    val name: String,
    val totalBudget: Double,
    val dailyLimit: Double,
    val durationDays: Int,
    val startDate: Instant,
    val createdAt: Instant
) {
    val endDate: Instant
        get() = Instant.fromEpochMilliseconds(startDate.toEpochMilliseconds() + (durationDays * 24L * 60 * 60 * 1000))
    
    val isActive: Boolean
        get() = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() < endDate.toEpochMilliseconds()
}
