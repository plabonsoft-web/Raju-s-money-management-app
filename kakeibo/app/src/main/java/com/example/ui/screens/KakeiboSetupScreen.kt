package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.FixedExpenseItem
import com.example.ui.viewmodel.KakeiboUiState
import com.example.ui.viewmodel.KakeiboViewModel
import java.util.Locale

@Composable
fun KakeiboSetupScreen(
    state: KakeiboUiState,
    viewModel: KakeiboViewModel,
    onBudgetSaved: () -> Unit
) {
    var incomeInput by remember { mutableStateOf("") }
    var savingsGoalInput by remember { mutableStateOf("") }
    var carryForwardChecked by remember { mutableStateOf(true) }

    val fixedItems = remember { mutableStateListOf<FixedExpenseItem>() }

    // Dialog state to add custom fixed expense
    var showAddFixedDialog by remember { mutableStateOf(false) }
    var newFixedName by remember { mutableStateOf("") }
    var newFixedAmount by remember { mutableStateOf("") }

    LaunchedEffect(state.budget) {
        state.budget?.let { b ->
            incomeInput = if (b.totalIncome > 0) b.totalIncome.toString() else "3200"
            savingsGoalInput = if (b.savingsGoal > 0) b.savingsGoal.toString() else "600"
            carryForwardChecked = b.carryForwardEnabled
            fixedItems.clear()
            fixedItems.addAll(viewModel.parseFixedExpenses(b.fixedExpensesJson))
            if (fixedItems.isEmpty()) {
                fixedItems.addAll(
                    listOf(
                        FixedExpenseItem("Rent / Mortgage", 950.0),
                        FixedExpenseItem("Utilities & Wi-Fi", 180.0),
                        FixedExpenseItem("Phone & Subscriptions", 120.0),
                        FixedExpenseItem("Insurance", 200.0)
                    )
                )
            }
        }
    }

    val incomeVal = incomeInput.toDoubleOrNull() ?: 0.0
    val savingsVal = savingsGoalInput.toDoubleOrNull() ?: 0.0
    val fixedTotal = fixedItems.sumOf { it.amount }
    val availableSpending = (incomeVal - fixedTotal - savingsVal).coerceAtLeast(0.0)
    val dailyAllowance = availableSpending / 30.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Japanese Kakeibo Introduction Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        RoundedCornerShape(20.dp)
                    ),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "TRADITIONAL KAKEIBO METHOD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "The 4 Core Questions (4つの問い)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = state.currentMonth,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Invented in 1904 by Hani Motoko, Kakeibo teaches mindful financial control. You set aside your savings first, deduct mandatory fixed bills, and live mindfully on the remaining daily spending allowance.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Question 1: Total Monthly Income
        item {
            KakeiboQuestionCard(
                questionNumber = "1",
                kanji = "収入",
                title = "Total Monthly Income",
                subtitle = "How much money do you expect this month?"
            ) {
                OutlinedTextField(
                    value = incomeInput,
                    onValueChange = { incomeInput = it },
                    label = { Text("Expected Income (${state.activeCurrency})") },
                    prefix = { Text(state.activeCurrency) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_input")
                )
            }
        }

        // Question 2: Fixed Expenses Breakdown
        item {
            KakeiboQuestionCard(
                questionNumber = "2",
                kanji = "固定費",
                title = "Fixed Expenses",
                subtitle = "Mandatory recurring bills (Rent, utilities, subscriptions)",
                trailing = {
                    Text(
                        text = "Total: ${state.activeCurrency}${String.format(Locale.US, "%.0f", fixedTotal)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (item in fixedItems) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${state.activeCurrency}${String.format(Locale.US, "%.2f", item.amount)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            IconButton(
                                onClick = { fixedItems.remove(item) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Button to add fixed bill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddFixedDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add Fixed Bill",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Question 3: Savings Goal (Set aside first!)
        item {
            KakeiboQuestionCard(
                questionNumber = "3",
                kanji = "先取り貯蓄",
                title = "Savings Goal (Set Aside First)",
                subtitle = "The Kakeibo golden rule: save before you start spending!"
            ) {
                OutlinedTextField(
                    value = savingsGoalInput,
                    onValueChange = { savingsGoalInput = it },
                    label = { Text("Monthly Savings Target (${state.activeCurrency})") },
                    prefix = { Text(state.activeCurrency) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_goal_input")
                )
            }
        }

        // Question 4: Available Spending Money (Calculated)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
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
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "QUESTION 4 • 使えるお金",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Available Spending Money",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(
                            text = "${state.activeCurrency}${String.format(Locale.US, "%.2f", availableSpending)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Formula: Total Income (${state.activeCurrency}${String.format(Locale.US, "%.0f", incomeVal)}) − Fixed Bills (${state.activeCurrency}${String.format(Locale.US, "%.0f", fixedTotal)}) − Savings (${state.activeCurrency}${String.format(Locale.US, "%.0f", savingsVal)})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily allowance badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Pocket Allowance:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${state.activeCurrency}${String.format(Locale.US, "%.2f", dailyAllowance)} / day",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Carry-Forward Rule Setting
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Carry-Forward Balance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Unspent daily budget carries forward as bonus allowance to the next day (or deficit if overspent).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = carryForwardChecked,
                        onCheckedChange = { carryForwardChecked = it }
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    viewModel.saveMonthlyBudget(
                        income = incomeVal,
                        fixedItems = fixedItems,
                        savingsGoal = savingsVal,
                        carryForward = carryForwardChecked
                    )
                    onBudgetSaved()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_kakeibo_budget_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Monthly Kakeibo Plan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Add Fixed Item Dialog
    if (showAddFixedDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddFixedDialog = false },
            title = { Text("Add Fixed Bill / Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newFixedName,
                        onValueChange = { newFixedName = it },
                        label = { Text("Bill Name (e.g. Gym, Internet, Car)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newFixedAmount,
                        onValueChange = { newFixedAmount = it },
                        label = { Text("Amount (${state.activeCurrency})") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = newFixedAmount.toDoubleOrNull() ?: 0.0
                        if (newFixedName.isNotBlank() && amt > 0.0) {
                            fixedItems.add(FixedExpenseItem(newFixedName.trim(), amt))
                            newFixedName = ""
                            newFixedAmount = ""
                            showAddFixedDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showAddFixedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun KakeiboQuestionCard(
    questionNumber: String,
    kanji: String,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(20.dp)
            ),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = questionNumber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$kanji • $title",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                trailing?.invoke()
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
