package com.example.zorvyn.data.repository

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.domain.repository.TransactionRepository
import com.example.zorvyn.database.AppDatabase
import com.example.zorvyn.database.TransactionEntity
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Duration.Companion.days

class TransactionRepositoryImpl(
    private val firestore: FirebaseFirestore,
    database: AppDatabase
) : TransactionRepository {
    private val collection = firestore.collection("transactions")
    private val queries = database.appDatabaseQueries
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            try {
                collection.snapshots.collect { snapshot ->
                    val remote = snapshot.documents.map { it.data<Transaction>() }
                    remote.forEach { 
                        queries.insertTransaction(
                            it.id, it.amount, it.note, it.type.name, 
                            it.category.name, it.timestamp.toEpochMilliseconds(), it.receiptPath
                        )
                    }
                }
            } catch (e: Exception) {}
        }
    }

    override fun getTransactions(): Flow<List<Transaction>> = 
        queries.getTransactions().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addTransaction(transaction: Transaction) {
        queries.insertTransaction(
            transaction.id, transaction.amount, transaction.note, transaction.type.name,
            transaction.category.name, transaction.timestamp.toEpochMilliseconds(), transaction.receiptPath
        )
        try {
            collection.document(transaction.id).set(transaction)
        } catch (e: Exception) {}
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        addTransaction(transaction)
    }

    override suspend fun deleteTransaction(id: String) {
        queries.deleteTransaction(id)
        try {
            collection.document(id).delete()
        } catch (e: Exception) {}
    }

    override suspend fun seedTransactionData() {
        val now = Clock.System.now()
        val mock = listOf(
            Transaction("t1", 65000.0, "Monthly Salary", TransactionType.INCOME, TransactionCategory.SALARY, now.minus(5.days)),
            Transaction("t2", 12000.0, "House Rent", TransactionType.EXPENSE, TransactionCategory.RENT, now.minus(4.days)),
            Transaction("t3", 2500.0, "Dinner with Friends", TransactionType.EXPENSE, TransactionCategory.FOOD, now.minus(3.days)),
            Transaction("t4", 1500.0, "Weekly Gas", TransactionType.EXPENSE, TransactionCategory.TRANSPORT, now.minus(2.days)),
            Transaction("t5", 850.0, "General Checkup", TransactionType.EXPENSE, TransactionCategory.HEALTHCARE, now.minus(1.days))
        )
        mock.forEach { addTransaction(it) }
    }

    private fun TransactionEntity.toDomain() = Transaction(
        id = id, amount = amount, note = note, 
        type = TransactionType.valueOf(type), 
        category = TransactionCategory.valueOf(category),
        timestamp = Instant.fromEpochMilliseconds(timestamp), 
        receiptPath = receiptPath
    )
}
