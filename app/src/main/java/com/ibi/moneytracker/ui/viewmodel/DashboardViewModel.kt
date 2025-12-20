package com.ibi.moneytracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibi.moneytracker.data.BillingCycle
import com.ibi.moneytracker.data.Expense
import com.ibi.moneytracker.data.ExpenseCategory
import com.ibi.moneytracker.data.ExpenseRepository
import com.ibi.moneytracker.data.FilterType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardViewModel(private val repository: ExpenseRepository) : ViewModel() {
    private val _currentCycleFilter = MutableStateFlow(FilterType.ALL)
    val currentCycleFilter: StateFlow<FilterType> = _currentCycleFilter
    private val _currentCategoryFilter = MutableStateFlow(FilterType.ALL.displayName)
    val currentCategoryFilter: StateFlow<String> = _currentCategoryFilter
    private val expensesFlow: Flow<List<Expense>> = repository.expenses
    val expensesList: StateFlow<List<Expense>> = combine(
        expensesFlow,
        _currentCycleFilter,
        _currentCategoryFilter
    ) { expenses, cycleFilter, categoryFilter ->
        val cycleFilteredList = when (cycleFilter) {
            FilterType.ALL -> expenses
            FilterType.MONTHLY -> expenses.filter { it.billingCycle == BillingCycle.MONTHLY }
            FilterType.YEARLY -> expenses.filter { it.billingCycle == BillingCycle.YEARLY }
            FilterType.ONE_TIME -> expenses.filter { it.billingCycle == BillingCycle.OneTimePayment }
        }
        return@combine when (categoryFilter) {
            "ALL" -> cycleFilteredList
            else -> cycleFilteredList.filter {
                it.category.displayName == categoryFilter
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )
    fun cycleFilter() {
        _currentCycleFilter.update { current ->
            val nextOrdinal = (current.ordinal + 1) % FilterType.entries.size
            FilterType.entries[nextOrdinal]
        }
    }
    fun setCategoryFilter(categoryName: String?) {
        _currentCategoryFilter.value = categoryName ?: "ALL"
    }

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
                    BillingCycle.OneTimePayment -> {
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

    val totalMonthlyYearlyIncludedCost: StateFlow<Double> = expensesFlow
        .map { list ->
            list.sumOf { exp ->
                val costAsDouble = exp.cost
                when (exp.billingCycle) {
                    BillingCycle.WEEKLY -> costAsDouble * (52.0 / 12.0)
                    BillingCycle.MONTHLY -> costAsDouble
                    BillingCycle.YEARLY -> costAsDouble / 12.0
                    BillingCycle.OneTimePayment -> costAsDouble
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalMonthlyCost: StateFlow<Double> = expensesFlow
        .map { list ->
            list.sumOf { exp ->
                val costAsDouble = exp.cost
                when (exp.billingCycle) {
                    BillingCycle.WEEKLY -> costAsDouble * (52.0 / 12.0)
                    BillingCycle.MONTHLY -> costAsDouble
                    BillingCycle.YEARLY -> (costAsDouble / 12.0) * 0
                    BillingCycle.OneTimePayment -> costAsDouble
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)


    val totalOneTimeCost: StateFlow<Double> = expensesFlow
        .map { list ->
            list.sumOf { exp ->
                val costAsDouble = exp.cost
                when (exp.billingCycle) {
                    BillingCycle.OneTimePayment -> costAsDouble
                    else -> 0.0
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun insertExpense(
        name: String,
        cost: Double,
        billingCycle: BillingCycle,
        category: ExpenseCategory,
        firstPaymentDate: LocalDate
    ) {
        viewModelScope.launch {
            val newExpense = Expense(
                name = name,
                cost = cost,
                billingCycle = billingCycle,
                category = category,
                firstPaymentDate = firstPaymentDate
            )
            repository.add(newExpense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun getExpenseById(expenseId: Int): Expense? {
        return expensesList.value.find { it.id == expenseId }
    }

    fun updateExpense(updatedExpense: Expense) {
        viewModelScope.launch {
            repository.update(updatedExpense)
        }
    }
}