package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
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

@Composable
fun QuickCalcPad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onDecimal: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick add buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val quickAdds = listOf("+1", "+5", "+10", "+20", "+50")
            for (qa in quickAdds) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clickable {
                            onOperator("+")
                            onDigit(qa.substring(1))
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = qa,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 1: 7, 8, 9, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(text = "7", modifier = Modifier.weight(1f), onClick = { onDigit("7") })
            CalcButton(text = "8", modifier = Modifier.weight(1f), onClick = { onDigit("8") })
            CalcButton(text = "9", modifier = Modifier.weight(1f), onClick = { onDigit("9") })
            CalcButton(
                text = "÷",
                modifier = Modifier.weight(1f),
                isOperator = true,
                onClick = { onOperator("/") }
            )
        }

        // Row 2: 4, 5, 6, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(text = "4", modifier = Modifier.weight(1f), onClick = { onDigit("4") })
            CalcButton(text = "5", modifier = Modifier.weight(1f), onClick = { onDigit("5") })
            CalcButton(text = "6", modifier = Modifier.weight(1f), onClick = { onDigit("6") })
            CalcButton(
                text = "×",
                modifier = Modifier.weight(1f),
                isOperator = true,
                onClick = { onOperator("*") }
            )
        }

        // Row 3: 1, 2, 3, -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(text = "1", modifier = Modifier.weight(1f), onClick = { onDigit("1") })
            CalcButton(text = "2", modifier = Modifier.weight(1f), onClick = { onDigit("2") })
            CalcButton(text = "3", modifier = Modifier.weight(1f), onClick = { onDigit("3") })
            CalcButton(
                text = "−",
                modifier = Modifier.weight(1f),
                isOperator = true,
                onClick = { onOperator("-") }
            )
        }

        // Row 4: C, 0, ., +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(
                text = "C",
                modifier = Modifier.weight(1f),
                isDanger = true,
                onClick = onClear
            )
            CalcButton(text = "0", modifier = Modifier.weight(1f), onClick = { onDigit("0") })
            CalcButton(text = ".", modifier = Modifier.weight(1f), onClick = onDecimal)
            CalcButton(
                text = "+",
                modifier = Modifier.weight(1f),
                isOperator = true,
                onClick = { onOperator("+") }
            )
        }

        // Row 5: Backspace & Evaluate Equals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onBackspace)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(3f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onEquals)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "= Calculate",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        isDanger -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isDanger -> MaterialTheme.colorScheme.error
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = if (isOperator) 20.sp else 18.sp,
            fontWeight = if (isOperator) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

object MathEvaluator {
    fun evaluate(expression: String): Double {
        if (expression.isBlank()) return 0.0
        // Clean characters
        val clean = expression.replace(" ", "").replace("×", "*").replace("÷", "/").replace("−", "-")
        return try {
            evalSimple(clean)
        } catch (_: Exception) {
            clean.toDoubleOrNull() ?: 0.0
        }
    }

    private fun evalSimple(str: String): Double {
        // Quick tokenizer for numbers and +, -, *, /
        val tokens = mutableListOf<String>()
        var currentNum = StringBuilder()
        for (i in str.indices) {
            val c = str[i]
            if (c.isDigit() || c == '.') {
                currentNum.append(c)
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                if (currentNum.isNotEmpty()) {
                    tokens.add(currentNum.toString())
                    currentNum = StringBuilder()
                } else if (c == '-' && tokens.isEmpty()) {
                    // leading negative number
                    currentNum.append(c)
                    continue
                }
                tokens.add(c.toString())
            }
        }
        if (currentNum.isNotEmpty()) {
            tokens.add(currentNum.toString())
        }

        if (tokens.isEmpty()) return 0.0
        if (tokens.size == 1) return tokens[0].toDoubleOrNull() ?: 0.0

        // Process * and / first
        val highPrecedenceTokens = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if ((token == "*" || token == "/") && highPrecedenceTokens.isNotEmpty() && i + 1 < tokens.size) {
                val left = highPrecedenceTokens.removeAt(highPrecedenceTokens.size - 1).toDoubleOrNull() ?: 0.0
                val right = tokens[i + 1].toDoubleOrNull() ?: 1.0
                val result = if (token == "*") left * right else if (right != 0.0) left / right else 0.0
                highPrecedenceTokens.add(result.toString())
                i += 2
            } else {
                highPrecedenceTokens.add(token)
                i++
            }
        }

        // Process + and -
        var result = highPrecedenceTokens[0].toDoubleOrNull() ?: 0.0
        var op = ""
        for (idx in 1 until highPrecedenceTokens.size) {
            val token = highPrecedenceTokens[idx]
            if (token == "+" || token == "-") {
                op = token
            } else {
                val nextVal = token.toDoubleOrNull() ?: 0.0
                if (op == "+") result += nextVal
                else if (op == "-") result -= nextVal
                op = ""
            }
        }
        return (Math.round(result * 100.0) / 100.0)
    }
}
