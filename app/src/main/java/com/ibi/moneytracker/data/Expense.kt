package com.ibi.moneytracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate

@TypeConverters(Converters::class)
@Entity(tableName = "expense")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val cost: Double,
    val billingCycle: BillingCycle,
    val category: ExpenseCategory,
    val firstPaymentDate: LocalDate
)
