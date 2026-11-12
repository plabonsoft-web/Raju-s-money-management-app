package com.example

import com.example.ui.components.MathEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun mathEvaluator_evaluatesBasicAddition() {
        assertEquals(25.0, MathEvaluator.evaluate("15 + 10"), 0.001)
    }

    @Test
    fun mathEvaluator_evaluatesDecimalsAndPrecedence() {
        assertEquals(22.5, MathEvaluator.evaluate("12.5 + 5 * 2"), 0.001)
    }

    @Test
    fun mathEvaluator_evaluatesDivisionAndSubtraction() {
        assertEquals(8.0, MathEvaluator.evaluate("20 - 24 / 2"), 0.001)
    }

    @Test
    fun kakeibo_availableSpendingCalculation() {
        val income = 3500.0
        val fixedExpenses = 1500.0
        val savingsGoal = 700.0
        val available = (income - fixedExpenses - savingsGoal).coerceAtLeast(0.0)
        assertEquals(1300.0, available, 0.001)
        val dailyAllowance = available / 30.0
        assertEquals(43.333, dailyAllowance, 0.01)
    }
}
