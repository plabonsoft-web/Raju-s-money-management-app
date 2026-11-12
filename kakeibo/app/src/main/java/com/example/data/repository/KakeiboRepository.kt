package com.example.data.repository

import com.example.data.local.KakeiboDao
import com.example.data.model.KakeiboPillar
import com.example.data.model.MonthlyBudget
import com.example.data.model.ReflectionJournal
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.Wallet
import com.example.data.model.WalletType
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class KakeiboRepository(private val dao: KakeiboDao) {

    val allWallets: Flow<List<Wallet>> = dao.getAllWallets()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allJournals: Flow<List<ReflectionJournal>> = dao.getAllJournals()

    fun getBudgetForMonth(yearMonth: String): Flow<MonthlyBudget?> =
        dao.getBudgetForMonth(yearMonth)

    fun getTransactionsForMonth(yearMonth: String): Flow<List<TransactionEntity>> =
        dao.getTransactionsForMonth(yearMonth)

    fun getTransactionsForDay(dateStr: String): Flow<List<TransactionEntity>> =
        dao.getTransactionsForDay(dateStr)

    suspend fun checkAndSeedInitialData() {
        val currentMonth = getCurrentYearMonth()
        val existingBudget = dao.getBudgetForMonthSync(currentMonth)
        if (existingBudget == null) {
            val defaultBudget = MonthlyBudget(
                yearMonth = currentMonth,
                totalIncome = 3200.0,
                fixedExpenses = 1450.0,
                savingsGoal = 600.0,
                availableSpending = 1150.0,
                fixedExpensesJson = """[{"name":"Rent / Mortgage","amount":950.0},{"name":"Utilities & Wi-Fi","amount":180.0},{"name":"Phone & Subscriptions","amount":120.0},{"name":"Insurance","amount":200.0}]""",
                carryForwardEnabled = true
            )
            dao.insertOrUpdateBudget(defaultBudget)
        }
    }

    suspend fun seedInitialWalletsIfEmpty() {
        val wallets = listOf(
            Wallet(name = "Daily Cash", type = WalletType.CASH, balance = 180.0, currencySymbol = "$", colorHex = 0xFFD97706, isDefault = true),
            Wallet(name = "Checking Account", type = WalletType.BANK, balance = 2450.0, currencySymbol = "$", colorHex = 0xFF2A3547),
            Wallet(name = "Mobile Pay", type = WalletType.MOBILE_WALLET, balance = 120.0, currencySymbol = "$", colorHex = 0xFF4338CA),
            Wallet(name = "Emergency Jar", type = WalletType.SAVINGS, balance = 1500.0, currencySymbol = "$", colorHex = 0xFF047857)
        )
        dao.insertWallets(wallets)
    }

    suspend fun addTransaction(tx: TransactionEntity): Long {
        val id = dao.insertTransaction(tx)
        // Adjust wallet balances
        when (tx.type) {
            TransactionType.EXPENSE -> {
                dao.adjustWalletBalance(tx.walletId, -tx.amount)
            }
            TransactionType.INCOME -> {
                dao.adjustWalletBalance(tx.walletId, tx.amount)
            }
            TransactionType.TRANSFER -> {
                val totalDeducted = tx.amount + tx.atmFee
                dao.adjustWalletBalance(tx.walletId, -totalDeducted)
                tx.toWalletId?.let { targetId ->
                    dao.adjustWalletBalance(targetId, tx.amount)
                }
            }
        }
        return id
    }

    suspend fun deleteTransaction(tx: TransactionEntity) {
        dao.deleteTransaction(tx.id)
        // Reverse balance
        when (tx.type) {
            TransactionType.EXPENSE -> {
                dao.adjustWalletBalance(tx.walletId, tx.amount)
            }
            TransactionType.INCOME -> {
                dao.adjustWalletBalance(tx.walletId, -tx.amount)
            }
            TransactionType.TRANSFER -> {
                val totalDeducted = tx.amount + tx.atmFee
                dao.adjustWalletBalance(tx.walletId, totalDeducted)
                tx.toWalletId?.let { targetId ->
                    dao.adjustWalletBalance(targetId, -tx.amount)
                }
            }
        }
    }

    suspend fun saveBudget(budget: MonthlyBudget) {
        dao.insertOrUpdateBudget(budget)
    }

    suspend fun addWallet(wallet: Wallet): Long = dao.insertWallet(wallet)
    suspend fun updateWallet(wallet: Wallet) = dao.updateWallet(wallet)
    suspend fun deleteWallet(id: Long) = dao.deleteWallet(id)

    suspend fun addJournal(journal: ReflectionJournal): Long = dao.insertJournal(journal)
    suspend fun deleteJournal(id: Long) = dao.deleteJournal(id)

    companion object {
        fun getCurrentYearMonth(): String {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            return sdf.format(Calendar.getInstance().time)
        }

        fun getCurrentDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Calendar.getInstance().time)
        }
    }
}
