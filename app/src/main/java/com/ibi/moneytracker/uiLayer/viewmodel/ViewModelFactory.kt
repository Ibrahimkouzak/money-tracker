package com.ibi.moneytracker.uiLayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ibi.moneytracker.dataLayer.ExpenseRepository

class ViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AddEditViewModel::class.java) -> {
                AddEditViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ExpensesViewModel::class.java) -> {
                ExpensesViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ChartViewViewModel::class.java) -> {
                ChartViewViewModel(repository) as T
            }
            // Add other ViewModels here as they are implemented
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
