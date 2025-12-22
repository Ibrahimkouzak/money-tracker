package com.ibi.moneytracker.uiLayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibi.moneytracker.uiLayer.data.BillingCycle
import com.ibi.moneytracker.uiLayer.data.Expense
import com.ibi.moneytracker.uiLayer.data.ExpenseCategory
import com.ibi.moneytracker.dataLayer.ExpenseRepository
import com.ibi.moneytracker.uiLayer.data.FilterType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddEditViewModel(private val repository: ExpenseRepository) : ViewModel() {
    private val _currentCycleFilter = MutableStateFlow(FilterType.ALL)
    private val _currentCategoryFilter = MutableStateFlow(FilterType.ALL.displayName)
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

    fun updateExpense(updatedExpense: Expense) {
        viewModelScope.launch {
            repository.update(updatedExpense)
        }
    }
}