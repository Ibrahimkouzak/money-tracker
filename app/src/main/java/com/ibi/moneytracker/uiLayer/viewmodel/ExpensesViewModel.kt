package com.ibi.moneytracker.uiLayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibi.moneytracker.uiLayer.data.BillingCycle
import com.ibi.moneytracker.uiLayer.data.Expense
import com.ibi.moneytracker.dataLayer.ExpenseRepository
import com.ibi.moneytracker.uiLayer.data.FilterType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpensesViewModel(private val repository: ExpenseRepository) : ViewModel()  {

    private val expensesFlow: Flow<List<Expense>> = repository.expenses
    private val _currentCycleFilter = MutableStateFlow(FilterType.ALL)
    private val _currentCategoryFilter = MutableStateFlow(FilterType.ALL.displayName)
    val currentCycleFilter: StateFlow<FilterType> = _currentCycleFilter
    val currentCategoryFilter: StateFlow<String> = _currentCategoryFilter
    val expensesList: StateFlow<List<Expense>> = combine(
        expensesFlow,
        _currentCycleFilter,
        _currentCategoryFilter
    ) { expenses, cycleFilter, categoryFilter ->
        val cycleFilteredList = when (cycleFilter) {
            FilterType.ALL -> expenses
            FilterType.MONTHLY -> expenses.filter { it.billingCycle == BillingCycle.MONTHLY }
            FilterType.YEARLY -> expenses.filter { it.billingCycle == BillingCycle.YEARLY }
            FilterType.ONE_TIME -> expenses.filter { it.billingCycle == BillingCycle.ONE_TIME }
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

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }
}