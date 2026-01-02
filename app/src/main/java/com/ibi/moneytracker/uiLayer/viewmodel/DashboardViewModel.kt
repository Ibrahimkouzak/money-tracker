package com.ibi.moneytracker.uiLayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibi.moneytracker.uiLayer.data.BillingCycle
import com.ibi.moneytracker.uiLayer.data.Expense
import com.ibi.moneytracker.dataLayer.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val expensesFlow: Flow<List<Expense>> = repository.expenses
    val totalMonthlyYearlyIncludedCost: StateFlow<Double> = expensesFlow
        .map { list ->
            list.sumOf { exp ->
                val costAsDouble = exp.cost
                when (exp.billingCycle) {
                    BillingCycle.WEEKLY -> costAsDouble * (52.0 / 12.0)
                    BillingCycle.MONTHLY -> costAsDouble
                    BillingCycle.YEARLY -> costAsDouble / 12.0
                    BillingCycle.ONE_TIME -> costAsDouble
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
                    BillingCycle.ONE_TIME -> costAsDouble
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)


    val totalOneTimeCost: StateFlow<Double> = expensesFlow
        .map { list ->
            list.sumOf { exp ->
                val costAsDouble = exp.cost
                when (exp.billingCycle) {
                    BillingCycle.ONE_TIME -> costAsDouble
                    else -> 0.0
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)
}