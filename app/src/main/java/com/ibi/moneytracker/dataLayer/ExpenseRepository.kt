package com.ibi.moneytracker.dataLayer

import com.ibi.moneytracker.uiLayer.data.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {
    val expenses: Flow<List<Expense>> = dao.getAllExpenses()
    suspend fun add(expense: Expense) = dao.insertExpense(expense)
    suspend fun update(expense: Expense) = dao.updateExpense(expense)
    suspend fun delete(expense: Expense) = dao.deleteExpense(expense)

}