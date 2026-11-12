package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IncomeGreen
import java.util.Locale

@Composable
fun CarryForwardCard(
    dateLabel: String,
    baseDailyBudget: Double,
    carryForwardDelta: Double,
    todaySpent: Double,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val totalTodayAllowance = (baseDailyBudget + carryForwardDelta).coerceAtLeast(0.0)
    val todayRemaining = totalTodayAllowance - todaySpent
    val isSurplus = carryForwardDelta >= 0
    val progress = if (totalTodayAllowance > 0) {
        (todaySpent / totalTodayAllowance).toFloat().coerceIn(0f, 1f)
    } else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(20.dp)
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header with date and Japanese pocket stamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY'S TALLY ALLOWANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = dateLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Pocket stamp badge (黒字 Surplus or 節約 Alert)
                val stampText = if (todayRemaining >= 0) "黒字 Surplus" else "超過 Over"
                val stampColor = if (todayRemaining >= 0) IncomeGreen else MaterialTheme.colorScheme.error

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(stampColor.copy(alpha = 0.12f))
                        .border(1.dp, stampColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stampText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = stampColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Remaining Today Number
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.2f", todayRemaining)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (todayRemaining >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "remaining today",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Carry-forward breakdown pill row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Base
                Column {
                    Text(
                        text = "Base Daily",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", baseDailyBudget)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Carry-Forward
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Carry-Forward",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSurplus) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isSurplus) IncomeGreen else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = (if (isSurplus) "+" else "") + "$currencySymbol${String.format(Locale.US, "%.2f", carryForwardDelta)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSurplus) IncomeGreen else MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Spent Today
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Spent Today",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", todaySpent)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
