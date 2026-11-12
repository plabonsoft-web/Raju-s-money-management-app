package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class KakeiboPillar(
    val title: String,
    val kanji: String,
    val description: String,
    val colorHex: Long,
    val darkColorHex: Long
) {
    NEEDS(
        title = "Needs",
        kanji = "生存",
        description = "Survival: Groceries, medicine, transit",
        colorHex = 0xFFD97706,      // Amber 600
        darkColorHex = 0xFFF59E0B  // Amber 500
    ),
    WANTS(
        title = "Wants",
        kanji = "選択",
        description = "Optional: Dining, coffee, shopping",
        colorHex = 0xFFBE185D,      // Pink 700
        darkColorHex = 0xFFF43F5E  // Rose 500
    ),
    CULTURE(
        title = "Culture",
        kanji = "教養",
        description = "Growth: Books, courses, museums",
        colorHex = 0xFF4338CA,      // Indigo 700
        darkColorHex = 0xFF818CF8  // Indigo 400
    ),
    UNEXPECTED(
        title = "Unexpected",
        kanji = "特別",
        description = "Extra: Urgent repairs, gifts, health",
        colorHex = 0xFF047857,      // Emerald 700
        darkColorHex = 0xFF34D399  // Emerald 400
    )
}

enum class WalletType(val displayName: String) {
    CASH("Cash"),
    BANK("Bank Account"),
    MOBILE_WALLET("Mobile Wallet"),
    SAVINGS("Savings Jar")
}

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: WalletType = WalletType.CASH,
    val balance: Double = 0.0,
    val currencySymbol: String = "$",
    val colorHex: Long = 0xFF2A3547,
    val isDefault: Boolean = false
)

@Entity(tableName = "monthly_budgets")
data class MonthlyBudget(
    @PrimaryKey val yearMonth: String, // format "YYYY-MM", e.g. "2026-11"
    val totalIncome: Double = 0.0,     // Q1: Total monthly income
    val fixedExpenses: Double = 0.0,   // Q2: Fixed expenses sum (Rent, bills, subscriptions)
    val savingsGoal: Double = 0.0,     // Q3: Target savings set aside first
    val availableSpending: Double = 0.0,// Q4: TotalIncome - FixedExpenses - SavingsGoal
    val fixedExpensesJson: String = "[]", // list of fixed items in JSON: [{"name":"Rent","amount":1200}]
    val carryForwardEnabled: Boolean = true
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletId: Long,
    val toWalletId: Long? = null,
    val amount: Double,
    val type: TransactionType,
    val pillar: KakeiboPillar? = null,
    val payeeOrPayer: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "", // e.g. "2026-11-12"
    val atmFee: Double = 0.0,
    val hasReminder: Boolean = false,
    val reminderNote: String = ""
)

enum class PeriodType {
    WEEKLY,
    MONTHLY
}

@Entity(tableName = "reflection_journals")
data class ReflectionJournal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val yearMonth: String,
    val periodType: PeriodType = PeriodType.WEEKLY,
    val periodLabel: String, // e.g. "Week 2 (Nov 8 - Nov 14)" or "November 2026"
    val q1MoneyLeft: Double = 0.0,
    val q2SpendingReflection: String = "",
    val q3SavedGoalMet: Boolean = true,
    val q4ImprovementPlan: String = "",
    val rating: Int = 4, // 1 to 5 scale
    val createdAt: Long = System.currentTimeMillis()
)
