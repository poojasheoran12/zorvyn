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
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Duration.Companion.days

class FinancialRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val database: AppDatabase
) : FinancialRepository {
    private val transactionsCollection = firestore.collection("transactions")
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

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        amount = amount,
        note = note,
        type = TransactionType.valueOf(type),
        category = TransactionCategory.valueOf(category),
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        receiptPath = receiptPath
    )
}
