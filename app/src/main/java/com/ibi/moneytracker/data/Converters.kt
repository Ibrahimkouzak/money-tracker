package com.ibi.moneytracker.data

import androidx.room.TypeConverter
import java.time.LocalDate


class Converters {
    @TypeConverter
    fun fromBillingCycle(cycle: BillingCycle?): String? = cycle?.name

    @TypeConverter
    fun toBillingCycle(value: String?): BillingCycle? =
        value?.let { BillingCycle.valueOf(it) }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? =
        value?.let { LocalDate.ofEpochDay(it) }
}