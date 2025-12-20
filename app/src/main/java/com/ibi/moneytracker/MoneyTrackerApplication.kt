package com.ibi.moneytracker

import android.app.Application
import com.ibi.moneytracker.data.AppDatabase
import com.ibi.moneytracker.data.ExpenseRepository

class MoneyTrackerApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository: ExpenseRepository by lazy {
        ExpenseRepository(database.expenseDao())
    }
}