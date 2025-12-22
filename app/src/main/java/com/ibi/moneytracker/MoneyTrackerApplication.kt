package com.ibi.moneytracker

import android.app.Application
import com.ibi.moneytracker.dataLayer.AppDatabase
import com.ibi.moneytracker.dataLayer.ExpenseRepository

class MoneyTrackerApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
    val repository: ExpenseRepository by lazy {
        ExpenseRepository(database.expenseDao())
    }
}