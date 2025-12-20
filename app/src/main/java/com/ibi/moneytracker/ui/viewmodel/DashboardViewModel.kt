package com.ibi.moneytracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibi.moneytracker.data.BillingCycle
import com.ibi.moneytracker.data.Expense
import com.ibi.moneytracker.data.ExpenseCategory
import com.ibi.moneytracker.data.ExpenseRepository
import com.ibi.moneytracker.data.FilterType // Assuming this is the Billing Cycle filter
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

    // --- 1. Filter States ---

    // Billing Cycle Filter (cycled by button)
    private val _currentCycleFilter = MutableStateFlow(FilterType.ALL) // Renamed for clarity
    val currentCycleFilter: StateFlow<FilterType> = _currentCycleFilter

    // Category Filter (selected from dropdown)
    private val _currentCategoryFilter = MutableStateFlow(FilterType.ALL.displayName)
    val currentCategoryFilter: StateFlow<String> = _currentCategoryFilter // "All" is the default

    // --- Data Source ---

    private val expensesFlow: Flow<List<Expense>> = repository.expenses


    val allExpensesList: Flow<List<Expense>> = expensesFlow
    // --- Filtered List ---

    // Combines three flows: raw data, cycle filter, and category filter
    val expensesList: StateFlow<List<Expense>> = combine(
        expensesFlow,
        _currentCycleFilter,     // Billing Cycle
        _currentCategoryFilter   // Category
    ) { expenses, cycleFilter, categoryFilter ->

        // 1. Apply Billing Cycle Filter
        val cycleFilteredList = when (cycleFilter) {
            FilterType.ALL -> expenses
            FilterType.MONTHLY -> expenses.filter { it.billingCycle == BillingCycle.MONTHLY }
            FilterType.YEARLY -> expenses.filter { it.billingCycle == BillingCycle.YEARLY }
            FilterType.ONE_TIME -> expenses.filter { it.billingCycle == BillingCycle.OneTimePayment }
            // Note: If you have a WEEKLY filter, you'd add it here too.
        }

        // 2. Apply Category Filter
        return@combine when (categoryFilter) {
            "ALL" -> cycleFilteredList
            else -> cycleFilteredList.filter {
                // Assumes your Expense data class has a 'category' object with a 'name' field
                it.category.displayName == categoryFilter
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    // --- UI Event Handlers ---

    // Cycles the Billing Cycle filter
    fun cycleFilter() {
        _currentCycleFilter.update { current ->
            val nextOrdinal = (current.ordinal + 1) % FilterType.entries.size
            FilterType.entries[nextOrdinal]
        }
    }

    fun cycleFilterCategory() {
        _currentCategoryFilter.update { category ->
            when (category) {
                FilterType.ALL.name -> expensesList
                    .value
                    .map { it.billingCycle }
                    .distinct()
                    .joinToString { it.name }

                else -> category
            }
        }
    }

    // Sets the Category filter (called when a category is selected)
    fun setCategoryFilter(categoryName: String?) {
        _currentCategoryFilter.value = categoryName ?: "ALL"
    }

    // --- Aggregations (Now rely on the UNFILTERED expensesFlow) ---

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

    // --- CRUD Operations ---

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
        // Note: Using expensesList.value here will return a *filtered* list,
        // which might be okay, but using the raw flow is safer if you need all.
        // For now, this is fine.
        return expensesList.value.find { it.id == expenseId }
    }

    fun updateExpense(updatedExpense: Expense) {
        viewModelScope.launch {
            repository.update(updatedExpense)
        }
    }
}