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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.PeriodType
import com.example.data.model.ReflectionJournal
import com.example.ui.components.KakeiboPillarsChart
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.KakeiboUiState
import com.example.ui.viewmodel.KakeiboViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReflectionScreen(
    state: KakeiboUiState,
    viewModel: KakeiboViewModel
) {
    var showWriteDialog by remember { mutableStateOf(false) }

    val available = state.budget?.availableSpending ?: 0.0
    val moneyLeft = (available - state.monthTotalSpent).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reflection Introduction Card
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
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KAKEIBO JOURNAL (家計簿・省察)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Weekly & Monthly Reflection",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = { showWriteDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("write_reflection_button")
                        ) {
                            Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reflect", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Questions Quick Summary Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Available", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "${state.activeCurrency}${String.format(Locale.US, "%.0f", available)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Spent So Far", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "${state.activeCurrency}${String.format(Locale.US, "%.0f", state.monthTotalSpent)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Money Left", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "${state.activeCurrency}${String.format(Locale.US, "%.0f", moneyLeft)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }
                    }
                }
            }
        }

        // 4 Pillars Visual Donut & Distribution Chart
        item {
            KakeiboPillarsChart(
                needsTotal = state.needsTotal,
                wantsTotal = state.wantsTotal,
                cultureTotal = state.cultureTotal,
                unexpectedTotal = state.unexpectedTotal,
                currencySymbol = state.activeCurrency
            )
        }

        // Past Reflection Entries
        item {
            Text(
                text = "PAST REFLECTIONS (${state.journals.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (state.journals.isEmpty()) {
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
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Reflection Written Yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kakeibo works best with weekly or monthly pauses to examine how your spending aligned with your values. Tap 'Reflect' above to begin!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(state.journals, key = { it.id }) { journal ->
                ReflectionJournalCard(
                    journal = journal,
                    currency = state.activeCurrency,
                    onDelete = { viewModel.deleteJournal(journal.id) }
                )
            }
        }
    }

    // Write Reflection Modal Dialog
    if (showWriteDialog) {
        WriteReflectionDialog(
            currentPeriod = state.currentMonth,
            moneyLeft = moneyLeft,
            currency = state.activeCurrency,
            onDismiss = { showWriteDialog = false },
            onSave = { pType, label, q1, q2, q3, q4, rating ->
                viewModel.addReflectionJournal(pType, label, q1, q2, q3, q4, rating)
                showWriteDialog = false
            }
        )
    }
}

@Composable
private fun ReflectionJournalCard(
    journal: ReflectionJournal,
    currency: String,
    onDelete: () -> Unit
) {
    val dateStr = remember(journal.createdAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        sdf.format(Date(journal.createdAt))
    }

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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = journal.periodType.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = journal.periodLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(text = "Logged $dateStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Star rating
                    repeat(journal.rating) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reflection items
            if (journal.q2SpendingReflection.isNotBlank()) {
                Text(
                    text = "Q: How did I spend my money?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = journal.q2SpendingReflection,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (journal.q4ImprovementPlan.isNotBlank()) {
                Text(
                    text = "Q: How will I improve next time?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = journal.q4ImprovementPlan,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun WriteReflectionDialog(
    currentPeriod: String,
    moneyLeft: Double,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (PeriodType, String, Double, String, Boolean, String, Int) -> Unit
) {
    var periodType by remember { mutableStateOf(PeriodType.WEEKLY) }
    var periodLabel by remember { mutableStateOf(if (periodType == PeriodType.WEEKLY) "Week 2 ($currentPeriod)" else currentPeriod) }
    var q2Reflection by remember { mutableStateOf("") }
    var q3GoalMet by remember { mutableStateOf(true) }
    var q4Improvement by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(4) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Kakeibo Reflection Journal (省察)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Period Selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = periodType == PeriodType.WEEKLY,
                        onClick = {
                            periodType = PeriodType.WEEKLY
                            periodLabel = "Week ($currentPeriod)"
                        },
                        label = { Text("Weekly") }
                    )
                    FilterChip(
                        selected = periodType == PeriodType.MONTHLY,
                        onClick = {
                            periodType = PeriodType.MONTHLY
                            periodLabel = currentPeriod
                        },
                        label = { Text("Monthly") }
                    )
                }

                OutlinedTextField(
                    value = periodLabel,
                    onValueChange = { periodLabel = it },
                    label = { Text("Period Name / Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Q1: Money Left
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "1. Remaining Budget:", fontSize = 12.sp)
                        Text(
                            text = "$currency${String.format(Locale.US, "%.2f", moneyLeft)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }

                // Q2: How did I spend it?
                OutlinedTextField(
                    value = q2Reflection,
                    onValueChange = { q2Reflection = it },
                    label = { Text("2. How did you actually spend it? (Needs vs Wants)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Q3: Did you save what you set out to save?
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "3. Met Savings Target?", fontSize = 13.sp)
                    Switch(checked = q3GoalMet, onCheckedChange = { q3GoalMet = it })
                }

                // Q4: How will you improve next month?
                OutlinedTextField(
                    value = q4Improvement,
                    onValueChange = { q4Improvement = it },
                    label = { Text("4. Actionable improvements for next period:") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Mindfulness Rating:", fontSize = 12.sp)
                    Row {
                        (1..5).forEach { r ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$r stars",
                                tint = if (r <= rating) Color(0xFFEAB308) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { rating = r }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        periodType,
                        periodLabel,
                        moneyLeft,
                        q2Reflection,
                        q3GoalMet,
                        q4Improvement,
                        rating
                    )
                }
            ) {
                Text("Save Reflection")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
