package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MonthlyBudget
import com.example.data.model.ReflectionJournal
import com.example.data.model.TransactionEntity
import com.example.data.model.Wallet
import kotlinx.coroutines.flow.Flow

@Dao
interface KakeiboDao {
    // --- Wallets ---
    @Query("SELECT * FROM wallets ORDER BY id ASC")
    fun getAllWallets(): Flow<List<Wallet>>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getWalletById(id: Long): Wallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: Wallet): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<Wallet>)

    @Update
    suspend fun updateWallet(wallet: Wallet)

    @Query("UPDATE wallets SET balance = balance + :delta WHERE id = :id")
    suspend fun adjustWalletBalance(id: Long, delta: Double)

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteWallet(id: Long)

    // --- Monthly Budgets ---
    @Query("SELECT * FROM monthly_budgets WHERE yearMonth = :yearMonth LIMIT 1")
    fun getBudgetForMonth(yearMonth: String): Flow<MonthlyBudget?>

    @Query("SELECT * FROM monthly_budgets WHERE yearMonth = :yearMonth LIMIT 1")
    suspend fun getBudgetForMonthSync(yearMonth: String): MonthlyBudget?

    @Query("SELECT * FROM monthly_budgets ORDER BY yearMonth DESC")
    fun getAllBudgets(): Flow<List<MonthlyBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: MonthlyBudget)

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateString LIKE :monthPrefix || '%' ORDER BY timestamp DESC")
    fun getTransactionsForMonth(monthPrefix: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateString = :dateStr ORDER BY timestamp DESC")
    fun getTransactionsForDay(dateStr: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    // --- Reflection Journals ---
    @Query("SELECT * FROM reflection_journals ORDER BY createdAt DESC")
    fun getAllJournals(): Flow<List<ReflectionJournal>>

    @Query("SELECT * FROM reflection_journals WHERE yearMonth = :yearMonth ORDER BY createdAt DESC")
    fun getJournalsForMonth(yearMonth: String): Flow<List<ReflectionJournal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: ReflectionJournal): Long

    @Query("DELETE FROM reflection_journals WHERE id = :id")
    suspend fun deleteJournal(id: Long)
}
