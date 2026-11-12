package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.KakeiboPillar
import com.example.data.model.MonthlyBudget
import com.example.data.model.PeriodType
import com.example.data.model.ReflectionJournal
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.Wallet
import com.example.data.model.WalletType
import com.example.data.repository.KakeiboRepository
import com.example.ui.components.MathEvaluator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class FixedExpenseItem(val name: String, val amount: Double)

data class KakeiboUiState(
    val wallets: List<Wallet> = emptyList(),
    val currentMonth: String = "",
    val selectedDate: String = "",
    val budget: MonthlyBudget? = null,
    val monthTransactions: List<TransactionEntity> = emptyList(),
    val dayTransactions: List<TransactionEntity> = emptyList(),
    val journals: List<ReflectionJournal> = emptyList(),
    val baseDailyBudget: Double = 0.0,
    val carryForwardDelta: Double = 0.0,
    val todaySpent: Double = 0.0,
    val needsTotal: Double = 0.0,
    val wantsTotal: Double = 0.0,
    val cultureTotal: Double = 0.0,
    val unexpectedTotal: Double = 0.0,
    val monthTotalSpent: Double = 0.0,
    val activeCurrency: String = "$"
)

class KakeiboViewModel(private val repository: KakeiboRepository) : ViewModel() {

    private val _currentMonth = MutableStateFlow(KakeiboRepository.getCurrentYearMonth())
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(KakeiboRepository.getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
        viewModelScope.launch {
            repository.allWallets.collect { list ->
                if (list.isEmpty()) {
                    repository.seedInitialWalletsIfEmpty()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentBudgetFlow = _currentMonth.flatMapLatest { month ->
        repository.getBudgetForMonth(month)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthTransactionsFlow = _currentMonth.flatMapLatest { month ->
        repository.getTransactionsForMonth(month)
    }

    private val baseInfoFlow = combine(
        repository.allWallets,
        _selectedDate,
        _currentMonth
    ) { wallets, date, month ->
        Triple(wallets, date, month)
    }

    val uiState: StateFlow<KakeiboUiState> = combine(
        baseInfoFlow,
        currentBudgetFlow,
        monthTransactionsFlow,
        repository.allJournals
    ) { (wallets, selectedDate, currentMonth), budget, monthTxs, journals ->
        val currency = wallets.firstOrNull { it.isDefault }?.currencySymbol
            ?: wallets.firstOrNull()?.currencySymbol ?: "$"

        val dayTxs = monthTxs.filter { it.dateString == selectedDate }

        // Pillar totals for the month
        var needs = 0.0
        var wants = 0.0
        var culture = 0.0
        var unexpected = 0.0
        var totalSpent = 0.0

        for (tx in monthTxs) {
            if (tx.type == TransactionType.EXPENSE) {
                totalSpent += tx.amount
                when (tx.pillar) {
                    KakeiboPillar.NEEDS -> needs += tx.amount
                    KakeiboPillar.WANTS -> wants += tx.amount
                    KakeiboPillar.CULTURE -> culture += tx.amount
                    KakeiboPillar.UNEXPECTED -> unexpected += tx.amount
                    null -> {}
                }
            }
        }

        // Days in month calculation
        val daysInMonth = getDaysInMonth(currentMonth)
        val availableSpending = budget?.availableSpending ?: 0.0
        val baseDaily = if (daysInMonth > 0) availableSpending / daysInMonth else 0.0

        // Calculate carry-forward:
        // Day of month (1..31)
        val currentDayNum = getDayOfMonth(selectedDate)
        var priorDaysSpent = 0.0
        for (tx in monthTxs) {
            if (tx.type == TransactionType.EXPENSE) {
                val txDay = getDayOfMonth(tx.dateString)
                if (txDay < currentDayNum) {
                    priorDaysSpent += tx.amount
                }
            }
        }

        val priorDaysCount = (currentDayNum - 1).coerceAtLeast(0)
        val priorAllowance = priorDaysCount * baseDaily
        val carryForward = if (budget?.carryForwardEnabled == true) {
            priorAllowance - priorDaysSpent
        } else 0.0

        val todaySpent = dayTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        KakeiboUiState(
            wallets = wallets,
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            budget = budget,
            monthTransactions = monthTxs,
            dayTransactions = dayTxs,
            journals = journals.filter { it.yearMonth == currentMonth },
            baseDailyBudget = baseDaily,
            carryForwardDelta = carryForward,
            todaySpent = todaySpent,
            needsTotal = needs,
            wantsTotal = wants,
            cultureTotal = culture,
            unexpectedTotal = unexpected,
            monthTotalSpent = totalSpent,
            activeCurrency = currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KakeiboUiState()
    )

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
        if (dateStr.length >= 7) {
            _currentMonth.value = dateStr.substring(0, 7)
        }
    }

    fun stepDay(offset: Int) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            sdf.parse(_selectedDate.value)?.let { cal.time = it }
            cal.add(Calendar.DAY_OF_YEAR, offset)
            val newDate = sdf.format(cal.time)
            setSelectedDate(newDate)
        } catch (_: Exception) {}
    }

    fun setMonth(yearMonth: String) {
        _currentMonth.value = yearMonth
        _selectedDate.value = "$yearMonth-01"
    }

    fun addQuickExpense(
        amountExpression: String,
        pillar: KakeiboPillar,
        payee: String,
        note: String,
        walletId: Long,
        hasReminder: Boolean = false,
        reminderNote: String = ""
    ) {
        val amount = MathEvaluator.evaluate(amountExpression)
        if (amount <= 0.0) return

        viewModelScope.launch {
            val tx = TransactionEntity(
                walletId = walletId,
                amount = amount,
                type = TransactionType.EXPENSE,
                pillar = pillar,
                payeeOrPayer = payee.trim(),
                note = note.trim(),
                timestamp = System.currentTimeMillis(),
                dateString = _selectedDate.value,
                hasReminder = hasReminder,
                reminderNote = reminderNote
            )
            repository.addTransaction(tx)
        }
    }

    fun addIncome(
        amountExpression: String,
        payer: String,
        note: String,
        walletId: Long
    ) {
        val amount = MathEvaluator.evaluate(amountExpression)
        if (amount <= 0.0) return

        viewModelScope.launch {
            val tx = TransactionEntity(
                walletId = walletId,
                amount = amount,
                type = TransactionType.INCOME,
                pillar = null,
                payeeOrPayer = payer.trim(),
                note = note.trim(),
                timestamp = System.currentTimeMillis(),
                dateString = _selectedDate.value
            )
            repository.addTransaction(tx)
        }
    }

    fun addTransfer(
        amountExpression: String,
        fromWalletId: Long,
        toWalletId: Long,
        atmFeeExpression: String = "0",
        note: String = ""
    ) {
        val amount = MathEvaluator.evaluate(amountExpression)
        val atmFee = MathEvaluator.evaluate(atmFeeExpression)
        if (amount <= 0.0 || fromWalletId == toWalletId) return

        viewModelScope.launch {
            val tx = TransactionEntity(
                walletId = fromWalletId,
                toWalletId = toWalletId,
                amount = amount,
                type = TransactionType.TRANSFER,
                pillar = null,
                payeeOrPayer = "Transfer",
                note = note.trim(),
                atmFee = atmFee,
                timestamp = System.currentTimeMillis(),
                dateString = _selectedDate.value
            )
            repository.addTransaction(tx)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun saveMonthlyBudget(
        income: Double,
        fixedItems: List<FixedExpenseItem>,
        savingsGoal: Double,
        carryForward: Boolean = true
    ) {
        val fixedTotal = fixedItems.sumOf { it.amount }
        val available = (income - fixedTotal - savingsGoal).coerceAtLeast(0.0)

        val jsonArray = JSONArray()
        for (item in fixedItems) {
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("amount", item.amount)
            jsonArray.put(obj)
        }

        val budget = MonthlyBudget(
            yearMonth = _currentMonth.value,
            totalIncome = income,
            fixedExpenses = fixedTotal,
            savingsGoal = savingsGoal,
            availableSpending = available,
            fixedExpensesJson = jsonArray.toString(),
            carryForwardEnabled = carryForward
        )

        viewModelScope.launch {
            repository.saveBudget(budget)
        }
    }

    fun parseFixedExpenses(json: String?): List<FixedExpenseItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<FixedExpenseItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(FixedExpenseItem(obj.getString("name"), obj.getDouble("amount")))
            }
        } catch (_: Exception) {}
        return list
    }

    fun addWallet(name: String, type: WalletType, initialBalance: Double, currency: String, colorHex: Long) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addWallet(
                Wallet(
                    name = name.trim(),
                    type = type,
                    balance = initialBalance,
                    currencySymbol = currency,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteWallet(id: Long) {
        viewModelScope.launch {
            repository.deleteWallet(id)
        }
    }

    fun addReflectionJournal(
        periodType: PeriodType,
        periodLabel: String,
        q1MoneyLeft: Double,
        q2Reflection: String,
        q3GoalMet: Boolean,
        q4Improvement: String,
        rating: Int
    ) {
        viewModelScope.launch {
            val journal = ReflectionJournal(
                yearMonth = _currentMonth.value,
                periodType = periodType,
                periodLabel = periodLabel.trim(),
                q1MoneyLeft = q1MoneyLeft,
                q2SpendingReflection = q2Reflection.trim(),
                q3SavedGoalMet = q3GoalMet,
                q4ImprovementPlan = q4Improvement.trim(),
                rating = rating
            )
            repository.addJournal(journal)
        }
    }

    fun deleteJournal(id: Long) {
        viewModelScope.launch {
            repository.deleteJournal(id)
        }
    }

    private fun getDaysInMonth(yearMonth: String): Int {
        return try {
            val parts = yearMonth.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        } catch (_: Exception) {
            30
        }
    }

    private fun getDayOfMonth(dateString: String): Int {
        return try {
            val parts = dateString.split("-")
            parts[2].toInt()
        } catch (_: Exception) {
            1
        }
    }
}

class KakeiboViewModelFactory(private val repository: KakeiboRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KakeiboViewModel::class.java)) {
            return KakeiboViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
