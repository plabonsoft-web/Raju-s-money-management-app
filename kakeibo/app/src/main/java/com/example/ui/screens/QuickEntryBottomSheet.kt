package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.KakeiboPillar
import com.example.data.model.TransactionType
import com.example.data.model.Wallet
import com.example.ui.components.MathEvaluator
import com.example.ui.components.PillarBadge
import com.example.ui.components.QuickCalcPad
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEntryBottomSheet(
    sheetState: SheetState,
    wallets: List<Wallet>,
    activeCurrency: String,
    onDismiss: () -> Unit,
    onSaveExpense: (amountExpr: String, pillar: KakeiboPillar, payee: String, note: String, walletId: Long, hasReminder: Boolean, reminderNote: String) -> Unit,
    onSaveIncome: (amountExpr: String, payer: String, note: String, walletId: Long) -> Unit,
    onSaveTransfer: (amountExpr: String, fromWalletId: Long, toWalletId: Long, atmFeeExpr: String, note: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Expense, 1: Income, 2: Transfer

    var amountExpr by remember { mutableStateOf("") }
    var selectedPillar by remember { mutableStateOf(KakeiboPillar.NEEDS) }
    var payeeOrPayer by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var hasReminder by remember { mutableStateOf(false) }
    var reminderNote by remember { mutableStateOf("") }

    val defaultWalletId = wallets.firstOrNull { it.isDefault }?.id ?: wallets.firstOrNull()?.id ?: 0L
    var selectedWalletId by remember { mutableLongStateOf(defaultWalletId) }
    var targetWalletId by remember {
        val second = wallets.getOrNull(1)?.id ?: defaultWalletId
        mutableLongStateOf(second)
    }
    var atmFeeExpr by remember { mutableStateOf("0") }

    val evaluatedValue = remember(amountExpr) {
        MathEvaluator.evaluate(amountExpr)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Type Selector Tabs (Expense / Income / Transfer)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Expense (支出)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Income (収入)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Transfer (移動)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount Display with Real-time Math Preview
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (amountExpr.isBlank()) "0" else amountExpr,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (selectedTab) {
                            0 -> MaterialTheme.colorScheme.onSurface
                            1 -> IncomeGreen
                            else -> TransferBlue
                        }
                    )
                    if (evaluatedValue > 0.0 && amountExpr != evaluatedValue.toString()) {
                        Text(
                            text = "= $activeCurrency${String.format(Locale.US, "%.2f", evaluatedValue)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // If Expense: 4 Kakeibo Pillars selector
            if (selectedTab == 0) {
                Text(
                    text = "SELECT KAKEIBO PILLAR (4大費目)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (pillar in KakeiboPillar.entries) {
                        PillarBadge(
                            pillar = pillar,
                            isSelected = selectedPillar == pillar,
                            onClick = { selectedPillar = pillar }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Wallet Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedTab == 2) "FROM ACCOUNT" else "ACCOUNT / WALLET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (w in wallets) {
                    val isSelected = w.id == selectedWalletId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedWalletId = w.id },
                        label = { Text("${w.name} ($activeCurrency${String.format(Locale.US, "%.0f", w.balance)})") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                }
            }

            // If Transfer: Target Wallet & ATM Fee
            if (selectedTab == 2) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "TO ACCOUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (w in wallets) {
                        if (w.id != selectedWalletId) {
                            val isSelected = w.id == targetWalletId
                            FilterChip(
                                selected = isSelected,
                                onClick = { targetWalletId = w.id },
                                label = { Text(w.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = atmFeeExpr,
                    onValueChange = { atmFeeExpr = it },
                    label = { Text("ATM Fee / Transfer Charge ($activeCurrency)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payee / Received From Field
            OutlinedTextField(
                value = payeeOrPayer,
                onValueChange = { payeeOrPayer = it },
                label = {
                    Text(
                        when (selectedTab) {
                            0 -> "Paid to (e.g. Supermarket, Cafe, Metro)"
                            1 -> "Received from (e.g. Salary, Client, Cash gift)"
                            else -> "Transfer Note"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("payee_input_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Note Field
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Short Note (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Reminder toggle for recurring or payment reminder
            if (selectedTab == 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Set Payment / Bill Reminder",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it }
                    )
                }
                if (hasReminder) {
                    OutlinedTextField(
                        value = reminderNote,
                        onValueChange = { reminderNote = it },
                        label = { Text("Reminder memo (e.g. Due every 15th)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Integrated Quick-Math Calculator Pad
            QuickCalcPad(
                onDigit = { d -> amountExpr += d },
                onOperator = { op ->
                    if (amountExpr.isNotEmpty() && !"+-*/".contains(amountExpr.last())) {
                        amountExpr += op
                    }
                },
                onDecimal = {
                    if (!amountExpr.endsWith(".")) amountExpr += "."
                },
                onBackspace = {
                    if (amountExpr.isNotEmpty()) {
                        amountExpr = amountExpr.dropLast(1)
                    }
                },
                onClear = { amountExpr = "" },
                onEquals = {
                    val res = MathEvaluator.evaluate(amountExpr)
                    if (res > 0.0) {
                        amountExpr = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Log Button
            Button(
                onClick = {
                    when (selectedTab) {
                        0 -> onSaveExpense(
                            amountExpr,
                            selectedPillar,
                            payeeOrPayer,
                            note,
                            selectedWalletId,
                            hasReminder,
                            reminderNote
                        )
                        1 -> onSaveIncome(amountExpr, payeeOrPayer, note, selectedWalletId)
                        2 -> onSaveTransfer(amountExpr, selectedWalletId, targetWalletId, atmFeeExpr, note)
                    }
                    onDismiss()
                },
                enabled = evaluatedValue > 0.0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedTab) {
                        0 -> Color(selectedPillar.colorHex)
                        1 -> IncomeGreen
                        else -> TransferBlue
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_transaction_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (selectedTab) {
                        0 -> "Log Expense ($activeCurrency${String.format(Locale.US, "%.2f", evaluatedValue)})"
                        1 -> "Log Income ($activeCurrency${String.format(Locale.US, "%.2f", evaluatedValue)})"
                        else -> "Record Transfer ($activeCurrency${String.format(Locale.US, "%.2f", evaluatedValue)})"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
