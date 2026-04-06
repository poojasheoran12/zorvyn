package com.example.zorvyn.data.repository

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.domain.repository.FinancialRepository
import com.example.zorvyn.database.AppDatabase
import com.example.zorvyn.database.TransactionEntity
import com.example.zorvyn.database.GoalEntity
import com.example.zorvyn.database.BudgetEntity
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Duration.Companion.days

class FinancialRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val database: AppDatabase
) : FinancialRepository {
    private val transactionsCollection = firestore.collection("transactions")
    private val goalsCollection = firestore.collection("goals")
    private val budgetsCollection = firestore.collection("budgets")
    private val queries = database.appDatabaseQueries
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        // Sync Firestore to Local SQLDelight
        repositoryScope.launch {
            try {
                transactionsCollection.snapshots.collect { snapshot ->
                    val remoteTransactions = snapshot.documents.map { it.data<Transaction>() }
                    // Update local DB
                    database.transaction {
                        remoteTransactions.forEach { 
                            queries.insertTransaction(
                                it.id, it.amount, it.note, it.type.name, 
                                it.category.name, it.timestamp.toEpochMilliseconds(), it.receiptPath
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle offline or error
            }
        }

        // Sync Firestore Goals to Local SQLDelight
        repositoryScope.launch {
            try {
                goalsCollection.snapshots.collect { snapshot ->
                    val remoteGoals = snapshot.documents.map { it.data<Goal>() }
                    database.transaction {
                        remoteGoals.forEach { 
                            queries.insertGoal(
                                it.id, it.name, it.targetAmount, it.savedAmount,
                                it.desiredDate.toEpochMilliseconds(), it.icon, it.createdAt.toEpochMilliseconds()
                            )
                        }
                    }
                }
            } catch (e: Exception) {}
        }

        // Sync Firestore Budgets to Local SQLDelight
        repositoryScope.launch {
            try {
                budgetsCollection.snapshots.collect { snapshot ->
                    val remoteBudgets = snapshot.documents.map { it.data<Budget>() }
                    database.transaction {
                        remoteBudgets.forEach { 
                            queries.insertBudget(
                                it.id, it.name, it.totalBudget, it.dailyLimit, 
                                it.durationDays.toLong(), it.startDate.toEpochMilliseconds(), it.createdAt.toEpochMilliseconds()
                            )
                        }
                    }
                }
            } catch (e: Exception) {}
        }
    }

    override fun getBalanceState(): Flow<BalanceState> = getTransactions().map { list ->
        val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val savings = income - expense
        val goal = 5000.0
        val progress = if (goal > 0) (savings / goal) else 0.0
        
        val streak = list.groupBy { 
            it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfYear 
        }.size.coerceAtMost(7)

        val feedback = when {
            progress >= 1.0 -> "Goal Reached! Amazing work! 🎯"
            progress >= 0.6 -> "You are ${ (progress * 100).toInt() }% closer to your goal 🎯"
            list.any { it.category == TransactionCategory.FOOD && it.amount > 1000 } -> 
                "Food spending increased this week ⚠️"
            savings > 0 -> "You spent less than yesterday 👏"
            else -> "Keep it up! Small steps lead to big reach."
        }

        BalanceState(
            currentBalance = savings,
            income = income,
            expense = expense,
            savings = savings,
            monthlyGoal = goal,
            streakDays = streak,
            smartFeedback = feedback
        )
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> = 
        queries.getTransactions().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.take(limit).map { it.toDomain() }
        }

    override fun getTransactions(
        type: TransactionType?,
        category: TransactionCategory?,
        limit: Int?
    ): Flow<List<Transaction>> = 
        queries.getTransactions().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.filter {
                (type == null || it.type == type.name) && 
                (category == null || it.category == category.name)
            }.let { filtered ->
                if (limit != null) filtered.take(limit) else filtered
            }.map { it.toDomain() }
        }

    override fun getCategorySummary(): Flow<Map<String, Double>> = getTransactions().map { list ->
        list.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category.name }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        // Local first
        queries.insertTransaction(
            transaction.id, transaction.amount, transaction.note, transaction.type.name,
            transaction.category.name, transaction.timestamp.toEpochMilliseconds(), transaction.receiptPath
        )
        // Remote second
        try {
            transactionsCollection.document(transaction.id).set(transaction)
        } catch (e: Exception) {
            // Firestore handles queueing locally if configured
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        addTransaction(transaction)
    }

    override suspend fun deleteTransaction(id: String) {
        queries.deleteTransaction(id)
        try {
            transactionsCollection.document(id).delete()
        } catch (e: Exception) {}
    }

    override fun exportToCsv(): Flow<String> = getTransactions().map { list ->
        val header = "ID,Amount,Note,Type,Category,Date\n"
        val rows = list.joinToString("\n") { 
            "${it.id},${it.amount},\"${it.note}\",${it.type},${it.category},${it.formattedDate}"
        }
        header + rows
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
            goalsCollection.document(goal.id).set(goal)
        } catch (e: Exception) {}
    }

    override suspend fun deleteGoal(id: String) {
        queries.deleteGoal(id)
        try {
            goalsCollection.document(id).delete()
        } catch (e: Exception) {}
    }

    override fun getBudgets(): Flow<List<Budget>> = 
        queries.getBudgets().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addBudget(budget: Budget) {
        queries.insertBudget(
            budget.id, budget.name, budget.totalBudget, budget.dailyLimit,
            budget.durationDays.toLong(), budget.startDate.toEpochMilliseconds(), budget.createdAt.toEpochMilliseconds()
        )
        try {
            budgetsCollection.document(budget.id).set(budget)
        } catch (e: Exception) {}
    }

    override suspend fun deleteBudget(id: String) {
        queries.deleteBudget(id)
        try {
            budgetsCollection.document(id).delete()
        } catch (e: Exception) {}
    }

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        amount = amount,
        note = note,
        type = TransactionType.valueOf(type),
        category = TransactionCategory.valueOf(category),
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        receiptPath = receiptPath
    )

    private fun GoalEntity.toDomain() = Goal(
        id = id,
        name = name,
        targetAmount = targetAmount,
        savedAmount = savedAmount,
        desiredDate = Instant.fromEpochMilliseconds(desiredDate),
        icon = icon,
        createdAt = Instant.fromEpochMilliseconds(createdAt)
    )

    private fun BudgetEntity.toDomain() = Budget(
        id = id,
        name = name,
        totalBudget = totalBudget,
        dailyLimit = dailyLimit,
        durationDays = durationDays.toInt(),
        startDate = Instant.fromEpochMilliseconds(startDate),
        createdAt = Instant.fromEpochMilliseconds(createdAt)
    )
}
