package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.Wallet
import com.example.data.repository.KakeiboRepository
import com.example.ui.components.CarryForwardCard
import com.example.ui.components.PillarBadge
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue
import com.example.ui.viewmodel.KakeiboUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TallyLedgerScreen(
    state: KakeiboUiState,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onGoToToday: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onOpenQuickEntry: () -> Unit
) {
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val formattedDate = remember(state.selectedDate) {
        formatDateJapaneseStyle(state.selectedDate)
    }

    val isToday = state.selectedDate == KakeiboRepository.getCurrentDateString()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenQuickEntry,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .testTag("add_transaction_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Entry",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Date Header & Navigation
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onPrevDay) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(onClick = onGoToToday)
                    ) {
                        Text(
                            text = formattedDate,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isToday) {
                            Text(
                                text = "Today's Ledger (本日)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Tap to jump to Today",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    IconButton(onClick = onNextDay) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Carry-Forward Balance Card
            item {
                CarryForwardCard(
                    dateLabel = formattedDate,
                    baseDailyBudget = state.baseDailyBudget,
                    carryForwardDelta = state.carryForwardDelta,
                    todaySpent = state.todaySpent,
                    currencySymbol = state.activeCurrency
                )
            }

            // Notebook Ledger Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENTRIES FOR THIS DAY (${state.dayTransactions.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Total Spent: ${state.activeCurrency}${String.format(Locale.US, "%.2f", state.todaySpent)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Ledger Entries List
            if (state.dayTransactions.isEmpty()) {
                item {
                    EmptyDayLedger(onQuickEntry = onOpenQuickEntry)
                }
            } else {
                items(state.dayTransactions, key = { it.id }) { tx ->
                    val wallet = state.wallets.firstOrNull { it.id == tx.walletId }
                    val targetWallet = tx.toWalletId?.let { targetId ->
                        state.wallets.firstOrNull { it.id == targetId }
                    }

                    NotebookTransactionRow(
                        transaction = tx,
                        wallet = wallet,
                        targetWallet = targetWallet,
                        currency = state.activeCurrency,
                        onDelete = { txToDelete = tx }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    txToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Delete Entry?") },
            text = {
                Text("Are you sure you want to delete this ${state.activeCurrency}${String.format(Locale.US, "%.2f", tx.amount)} entry? The wallet balance will be restored.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(tx)
                        txToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NotebookTransactionRow(
    transaction: TransactionEntity,
    wallet: Wallet?,
    targetWallet: Wallet?,
    currency: String,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            ),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left margin notebook indicator bar
            val barColor = when (transaction.type) {
                TransactionType.EXPENSE -> transaction.pillar?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.primary
                TransactionType.INCOME -> IncomeGreen
                TransactionType.TRANSFER -> TransferBlue
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.payeeOrPayer.ifBlank {
                            when (transaction.type) {
                                TransactionType.EXPENSE -> "Expense"
                                TransactionType.INCOME -> "Income"
                                TransactionType.TRANSFER -> "Transfer"
                            }
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (transaction.hasReminder) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reminder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (transaction.pillar != null && transaction.type == TransactionType.EXPENSE) {
                        PillarBadge(pillar = transaction.pillar)
                    } else if (transaction.type == TransactionType.TRANSFER) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TransferBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Transfer",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransferBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val accountText = when (transaction.type) {
                        TransactionType.TRANSFER -> "${wallet?.name ?: "Src"} → ${targetWallet?.name ?: "Dest"}"
                        else -> wallet?.name ?: "Cash"
                    }

                    Text(
                        text = "• $accountText",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (transaction.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.note,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Amount Display
            Column(horizontalAlignment = Alignment.End) {
                val prefix = when (transaction.type) {
                    TransactionType.EXPENSE -> "−"
                    TransactionType.INCOME -> "+"
                    TransactionType.TRANSFER -> "⇄"
                }

                val amountColor = when (transaction.type) {
                    TransactionType.EXPENSE -> MaterialTheme.colorScheme.onSurface
                    TransactionType.INCOME -> IncomeGreen
                    TransactionType.TRANSFER -> TransferBlue
                }

                Text(
                    text = "$prefix$currency${String.format(Locale.US, "%.2f", transaction.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                if (transaction.atmFee > 0.0) {
                    Text(
                        text = "+ fee $currency${String.format(Locale.US, "%.2f", transaction.atmFee)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDayLedger(onQuickEntry: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "No Entries for this Day",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Your daily allowance is intact! Tap the + button to quickly log an expense or income with the Kakeibo pillars.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.clickable(onClick = onQuickEntry)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Quick Log Expense",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

private fun formatDateJapaneseStyle(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = sdf.parse(dateStr) ?: return dateStr
        val cal = Calendar.getInstance()
        cal.time = date
        val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dayOfWeek = days[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val displaySdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        "${displaySdf.format(date)} ($dayOfWeek)"
    } catch (_: Exception) {
        dateStr
    }
}
