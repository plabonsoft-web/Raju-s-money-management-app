package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.KakeiboDatabase
import com.example.data.repository.KakeiboRepository
import com.example.ui.screens.KakeiboSetupScreen
import com.example.ui.screens.QuickEntryBottomSheet
import com.example.ui.screens.ReflectionScreen
import com.example.ui.screens.TallyLedgerScreen
import com.example.ui.screens.WalletsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.KakeiboViewModel
import com.example.ui.viewmodel.KakeiboViewModelFactory
import kotlinx.coroutines.launch

sealed class Screen(
    val title: String,
    val subtitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Tally : Screen(
        title = "Tally Ledger",
        subtitle = "Pocket Notebook",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
        testTag = "nav_tally_tab"
    )

    data object KakeiboSetup : Screen(
        title = "Kakeibo Budget",
        subtitle = "The 4 Questions",
        selectedIcon = Icons.Filled.Savings,
        unselectedIcon = Icons.Outlined.Savings,
        testTag = "nav_budget_tab"
    )

    data object Wallets : Screen(
        title = "Wallets & Accounts",
        subtitle = "Cash, Bank, Mobile",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
        testTag = "nav_wallets_tab"
    )

    data object Reflection : Screen(
        title = "Kakeibo Journal",
        subtitle = "Reflection & 4 Pillars",
        selectedIcon = Icons.Filled.AutoStories,
        unselectedIcon = Icons.Outlined.AutoStories,
        testTag = "nav_reflection_tab"
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = KakeiboDatabase.getDatabase(applicationContext)
        val repository = KakeiboRepository(database.kakeiboDao())

        setContent {
            MyApplicationTheme {
                KakeiboApp(repository = repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KakeiboApp(repository: KakeiboRepository) {
    val viewModel: KakeiboViewModel = viewModel(
        factory = KakeiboViewModelFactory(repository)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreenIndex by remember { mutableIntStateOf(0) }
    var isQuickEntryOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val screens = listOf(
        Screen.Tally,
        Screen.KakeiboSetup,
        Screen.Wallets,
        Screen.Reflection
    )
    val currentScreen = screens[currentScreenIndex]

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "家計簿",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentScreen.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                screens.forEachIndexed { index, screen ->
                    val isSelected = currentScreenIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreenIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title.substringBefore(" "),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Tally -> {
                    TallyLedgerScreen(
                        state = state,
                        onPrevDay = { viewModel.stepDay(-1) },
                        onNextDay = { viewModel.stepDay(1) },
                        onGoToToday = { viewModel.setSelectedDate(KakeiboRepository.getCurrentDateString()) },
                        onDeleteTransaction = { tx ->
                            viewModel.deleteTransaction(tx)
                            scope.launch {
                                snackbarHostState.showSnackbar("Entry deleted")
                            }
                        },
                        onOpenQuickEntry = { isQuickEntryOpen = true }
                    )
                }

                is Screen.KakeiboSetup -> {
                    KakeiboSetupScreen(
                        state = state,
                        viewModel = viewModel,
                        onBudgetSaved = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Kakeibo Monthly Plan saved!")
                            }
                        }
                    )
                }

                is Screen.Wallets -> {
                    WalletsScreen(
                        state = state,
                        viewModel = viewModel
                    )
                }

                is Screen.Reflection -> {
                    ReflectionScreen(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }

        // Quick Entry Bottom Sheet with Calculator
        if (isQuickEntryOpen) {
            QuickEntryBottomSheet(
                sheetState = sheetState,
                wallets = state.wallets,
                activeCurrency = state.activeCurrency,
                onDismiss = { isQuickEntryOpen = false },
                onSaveExpense = { amountExpr, pillar, payee, note, walletId, hasReminder, reminderNote ->
                    viewModel.addQuickExpense(
                        amountExpression = amountExpr,
                        pillar = pillar,
                        payee = payee,
                        note = note,
                        walletId = walletId,
                        hasReminder = hasReminder,
                        reminderNote = reminderNote
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar("Logged ${pillar.title} expense!")
                    }
                },
                onSaveIncome = { amountExpr, payer, note, walletId ->
                    viewModel.addIncome(
                        amountExpression = amountExpr,
                        payer = payer,
                        note = note,
                        walletId = walletId
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar("Logged income!")
                    }
                },
                onSaveTransfer = { amountExpr, fromWalletId, toWalletId, atmFeeExpr, note ->
                    viewModel.addTransfer(
                        amountExpression = amountExpr,
                        fromWalletId = fromWalletId,
                        toWalletId = toWalletId,
                        atmFeeExpression = atmFeeExpr,
                        note = note
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar("Transfer recorded!")
                    }
                }
            )
        }
    }
}
