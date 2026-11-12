package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KakeiboPillar
import java.util.Locale

@Composable
fun KakeiboPillarsChart(
    needsTotal: Double,
    wantsTotal: Double,
    cultureTotal: Double,
    unexpectedTotal: Double,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val total = needsTotal + wantsTotal + cultureTotal + unexpectedTotal

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
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
            Text(
                text = "THE 4 KAKEIBO PILLARS (4大費目)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (total <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses logged this period yet.\nLog your first expense below!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                // Donut Chart + Center Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            val strokeWidth = 18.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2f
                            val center = Offset(size.width / 2f, size.height / 2f)

                            val needsSweep = (needsTotal / total * 360f).toFloat()
                            val wantsSweep = (wantsTotal / total * 360f).toFloat()
                            val cultureSweep = (cultureTotal / total * 360f).toFloat()
                            val unexpectedSweep = (unexpectedTotal / total * 360f).toFloat()

                            var startAngle = -90f

                            val slices = listOf(
                                Pair(Color(KakeiboPillar.NEEDS.colorHex), needsSweep),
                                Pair(Color(KakeiboPillar.WANTS.colorHex), wantsSweep),
                                Pair(Color(KakeiboPillar.CULTURE.colorHex), cultureSweep),
                                Pair(Color(KakeiboPillar.UNEXPECTED.colorHex), unexpectedSweep)
                            )

                            for ((color, sweep) in slices) {
                                if (sweep > 0f) {
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep - (if (sweep < 360f) 2f else 0f),
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweep
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%.0f", total)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legend & percentages
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PillarLegendRow(
                            pillar = KakeiboPillar.NEEDS,
                            amount = needsTotal,
                            total = total,
                            currencySymbol = currencySymbol
                        )
                        PillarLegendRow(
                            pillar = KakeiboPillar.WANTS,
                            amount = wantsTotal,
                            total = total,
                            currencySymbol = currencySymbol
                        )
                        PillarLegendRow(
                            pillar = KakeiboPillar.CULTURE,
                            amount = cultureTotal,
                            total = total,
                            currencySymbol = currencySymbol
                        )
                        PillarLegendRow(
                            pillar = KakeiboPillar.UNEXPECTED,
                            amount = unexpectedTotal,
                            total = total,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PillarLegendRow(
    pillar: KakeiboPillar,
    amount: Double,
    total: Double,
    currencySymbol: String
) {
    val pct = if (total > 0) ((amount / total) * 100).toInt() else 0
    val color = Color(pillar.colorHex)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${pillar.kanji} ${pillar.title}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$currencySymbol${String.format(Locale.US, "%.0f", amount)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$pct%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
