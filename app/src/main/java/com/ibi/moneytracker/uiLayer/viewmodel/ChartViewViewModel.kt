package com.ibi.moneytracker.uiLayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibi.moneytracker.uiLayer.data.BillingCycle
import com.ibi.moneytracker.uiLayer.data.Expense
import com.ibi.moneytracker.uiLayer.data.ExpenseCategory
import com.ibi.moneytracker.dataLayer.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class ChartViewViewModel(private val repository: ExpenseRepository) : ViewModel()  {
    private val expensesFlow: Flow<List<Expense>> = repository.expenses

    val expensesByCategory: StateFlow<Map<ExpenseCategory, Double>> = expensesFlow
        .map { expenses ->
            expenses.groupBy { it.category }
                .mapValues { (_, groupedExpenses) ->
                    groupedExpenses.sumOf { it.cost }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    val dailyExpensesCurrentMonth: StateFlow<Map<Int, Double>> = expensesFlow
        .map { expenses ->
            val now = LocalDate.now()
            val daysInMonth = now.lengthOfMonth()
            val dailyTotals = mutableMapOf<Int, Double>()

            for (i in 1..daysInMonth) {
                dailyTotals[i] = 0.0
            }

            expenses.forEach { expense ->
                when (expense.billingCycle) {
                    BillingCycle.ONE_TIME -> {
                        if (expense.firstPaymentDate.month == now.month && expense.firstPaymentDate.year == now.year) {
                            val day = expense.firstPaymentDate.dayOfMonth
                            dailyTotals[day] = (dailyTotals[day] ?: 0.0) + expense.cost
                        }
                    }

                    BillingCycle.MONTHLY -> {
                        val day = expense.firstPaymentDate.dayOfMonth
                        val actualDay = if (day > daysInMonth) daysInMonth else day
                        dailyTotals[actualDay] = (dailyTotals[actualDay] ?: 0.0) + expense.cost
                    }

                    BillingCycle.WEEKLY -> {
                        var date = expense.firstPaymentDate
                        val monthStart = now.withDayOfMonth(1)
                        while (date.isBefore(monthStart)) {
                            date = date.plusWeeks(1)
                        }
                        while (date.month == now.month && date.year == now.year) {
                            val day = date.dayOfMonth
                            dailyTotals[day] = (dailyTotals[day] ?: 0.0) + expense.cost
                            date = date.plusWeeks(1)
                        }
                    }

                    BillingCycle.YEARLY -> {
                        if (expense.firstPaymentDate.month == now.month) {
                            val day = expense.firstPaymentDate.dayOfMonth
                            val actualDay = if (day > daysInMonth) daysInMonth else day
                            dailyTotals[actualDay] = (dailyTotals[actualDay] ?: 0.0) + expense.cost
                        }
                    }
                }
            }
            dailyTotals
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )
}