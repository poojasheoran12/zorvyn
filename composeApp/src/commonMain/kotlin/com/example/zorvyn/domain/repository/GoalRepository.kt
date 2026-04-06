package com.example.zorvyn.domain.repository

import kotlinx.coroutines.flow.Flow
import com.example.zorvyn.domain.model.Goal

interface GoalRepository {
    fun getGoals(): Flow<List<Goal>>
    suspend fun addGoal(goal: Goal)
    suspend fun deleteGoal(id: String)
    suspend fun seedGoalData()
}
