package com.example.zorvyn.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Goal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val desiredDate: Instant,
    val icon: String,
    val createdAt: Instant
) {
    val progress: Float
        get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    
    val isNearDeadline: Boolean
        get() = (desiredDate.toEpochMilliseconds() - kotlinx.datetime.Clock.System.now().toEpochMilliseconds()) < (7 * 24 * 60 * 60 * 1000) // 7 days
}
