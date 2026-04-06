package com.example.zorvyn.data.repository

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import com.example.zorvyn.domain.model.Goal
import com.example.zorvyn.domain.repository.GoalRepository
import com.example.zorvyn.database.AppDatabase
import com.example.zorvyn.database.GoalEntity
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Duration.Companion.days

class GoalRepositoryImpl(
    private val firestore: FirebaseFirestore,
    database: AppDatabase
) : GoalRepository {
    private val collection = firestore.collection("goals")
    private val queries = database.appDatabaseQueries
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            try {
                collection.snapshots.collect { snapshot ->
                    val remote = snapshot.documents.map { it.data<Goal>() }
                    remote.forEach { 
                        queries.insertGoal(
                            it.id, it.name, it.targetAmount, it.savedAmount,
                            it.desiredDate.toEpochMilliseconds(), it.icon, it.createdAt.toEpochMilliseconds()
                        )
                    }
                }
            } catch (e: Exception) {}
        }
    }

    override fun getGoals(): Flow<List<Goal>> = 
        queries.getGoals().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addGoal(goal: Goal) {
        queries.insertGoal(
            goal.id, goal.name, goal.targetAmount, goal.savedAmount,
            goal.desiredDate.toEpochMilliseconds(), goal.icon, goal.createdAt.toEpochMilliseconds()
        )
        try {
            collection.document(goal.id).set(goal)
        } catch (e: Exception) {}
    }

    override suspend fun deleteGoal(id: String) {
        queries.deleteGoal(id)
        try {
            collection.document(id).delete()
        } catch (e: Exception) {}
    }

    override suspend fun seedGoalData() {
        val now = Clock.System.now()
        val mock = listOf(
            Goal("g1", "New Car", 800000.0, 45000.0, now.plus(365.days), "🚗", now),
            Goal("g2", "Emergency Fund", 200000.0, 15000.0, now.plus(180.days), "💰", now)
        )
        mock.forEach { addGoal(it) }
    }

    private fun GoalEntity.toDomain() = Goal(
        id = id, name = name, targetAmount = targetAmount, 
        savedAmount = savedAmount,
        desiredDate = Instant.fromEpochMilliseconds(desiredDate), 
        icon = icon, createdAt = Instant.fromEpochMilliseconds(createdAt)
    )
}
