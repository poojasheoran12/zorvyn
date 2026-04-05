package com.example.zorvyn.domain.model

data class BalanceState(
    val currentBalance: Double,
    val income: Double,
    val expense: Double,
    val savings: Double,
    val monthlyGoal: Double = 5000.0,
    val streakDays: Int = 0,
    val smartFeedback: String = "Keep it up!"
)
