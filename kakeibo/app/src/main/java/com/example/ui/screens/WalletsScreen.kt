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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.Wallet
import com.example.data.model.WalletType
import com.example.ui.theme.TransferBlue
import com.example.ui.viewmodel.KakeiboUiState
import com.example.ui.viewmodel.KakeiboViewModel
import java.util.Locale

@Composable
fun WalletsScreen(
    state: KakeiboUiState,
    viewModel: KakeiboViewModel
) {
    var showTransferDialog by remember { mutableStateOf(false) }
    var showAddWalletDialog by remember { mutableStateOf(false) }

    val totalNetWorth = state.wallets.sumOf { it.balance }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Net Worth Banner
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
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "TOTAL ACCOUNTS BALANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${state.activeCurrency}${String.format(Locale.US, "%.2f", totalNetWorth)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showTransferDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TransferBlue),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("open_transfer_dialog_button")
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Transfer / ATM", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { showAddWalletDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("open_add_account_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Add Account",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "YOUR WALLETS & ACCOUNTS (${state.wallets.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        items(state.wallets, key = { it.id }) { wallet ->
            WalletCard(
                wallet = wallet,
                currency = state.activeCurrency,
                onDelete = {
                    if (state.wallets.size > 1) {
                        viewModel.deleteWallet(wallet.id)
                    }
                }
            )
        }
    }

    // Account Transfer Dialog
    if (showTransferDialog && state.wallets.size >= 2) {
        TransferDialog(
            wallets = state.wallets,
            currency = state.activeCurrency,
            onDismiss = { showTransferDialog = false },
            onTransfer = { amountExpr, fromId, toId, atmFeeExpr, note ->
                viewModel.addTransfer(amountExpr, fromId, toId, atmFeeExpr, note)
                showTransferDialog = false
            }
        )
    }

    // Add Wallet Dialog
    if (showAddWalletDialog) {
        AddWalletDialog(
            currency = state.activeCurrency,
            onDismiss = { showAddWalletDialog = false },
            onAdd = { name, type, balance, colorHex ->
                viewModel.addWallet(name, type, balance, state.activeCurrency, colorHex)
                showAddWalletDialog = false
            }
        )
    }
}

@Composable
private fun WalletCard(
    wallet: Wallet,
    currency: String,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp)
            ),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (wallet.type) {
                    WalletType.CASH -> Icons.Default.Payments
                    WalletType.BANK -> Icons.Default.AccountBalance
                    WalletType.MOBILE_WALLET -> Icons.Default.PhoneAndroid
                    WalletType.SAVINGS -> Icons.Default.Savings
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(wallet.colorHex).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(wallet.colorHex),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = wallet.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = wallet.type.displayName + if (wallet.isDefault) " • Primary" else "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currency${String.format(Locale.US, "%.2f", wallet.balance)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun TransferDialog(
    wallets: List<Wallet>,
    currency: String,
    onDismiss: () -> Unit,
    onTransfer: (amountExpr: String, fromId: Long, toId: Long, atmFeeExpr: String, note: String) -> Unit
) {
    var amountExpr by remember { mutableStateOf("") }
    var atmFeeExpr by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }

    var fromId by remember { mutableLongStateOf(wallets.first().id) }
    var toId by remember { mutableLongStateOf(wallets.getOrNull(1)?.id ?: wallets.first().id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account Transfer & ATM Fee") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Move funds between accounts or record ATM cash withdrawal.", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

                OutlinedTextField(
                    value = amountExpr,
                    onValueChange = { amountExpr = it },
                    label = { Text("Transfer Amount ($currency)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("From Account:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (w in wallets) {
                        FilterChip(
                            selected = w.id == fromId,
                            onClick = { fromId = w.id },
                            label = { Text(w.name) }
                        )
                    }
                }

                Text("To Account:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (w in wallets) {
                        if (w.id != fromId) {
                            FilterChip(
                                selected = w.id == toId,
                                onClick = { toId = w.id },
                                label = { Text(w.name) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = atmFeeExpr,
                    onValueChange = { atmFeeExpr = it },
                    label = { Text("ATM Fee / Service Charge ($currency)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (e.g. ATM withdrawal, rent transfer)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountExpr.isNotBlank() && fromId != toId) {
                        onTransfer(amountExpr, fromId, toId, atmFeeExpr, note)
                    }
                }
            ) {
                Text("Confirm Transfer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddWalletDialog(
    currency: String,
    onDismiss: () -> Unit,
    onAdd: (name: String, type: WalletType, balance: Double, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WalletType.CASH) }
    var initialBalance by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Wallet / Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (e.g. Suica Card, Second Bank)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Account Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (type in WalletType.entries) {
                        FilterChip(
                            selected = type == selectedType,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName) }
                        )
                    }
                }

                OutlinedTextField(
                    value = initialBalance,
                    onValueChange = { initialBalance = it },
                    label = { Text("Starting Balance ($currency)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = initialBalance.toDoubleOrNull() ?: 0.0
                    val colorHex = when (selectedType) {
                        WalletType.CASH -> 0xFFD97706
                        WalletType.BANK -> 0xFF2A3547
                        WalletType.MOBILE_WALLET -> 0xFF4338CA
                        WalletType.SAVINGS -> 0xFF047857
                    }
                    if (name.isNotBlank()) {
                        onAdd(name.trim(), selectedType, bal, colorHex)
                    }
                }
            ) {
                Text("Add Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
