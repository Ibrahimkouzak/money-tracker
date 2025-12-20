package com.ibi.moneytracker.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ibi.moneytracker.data.ExpenseCategory
import com.ibi.moneytracker.ui.viewmodel.DashboardViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry

@Composable
fun ChartView(modifier: Modifier, viewModel: DashboardViewModel) {
    val expensesByCategory by viewModel.expensesByCategory.collectAsStateWithLifecycle()
    val dailyExpenses by viewModel.dailyExpensesCurrentMonth.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
    ) {
        item {
            CategoryBarChart(
                expensesByCategory = expensesByCategory,
                modifier = modifier
            )
        }
        item {
            CategoryPieChart(
                expensesByCategory = expensesByCategory,
                modifier = modifier
            )
        }
        item {
            DailyExpensesLineChart(
                dailyExpenses = dailyExpenses,
                modifier = modifier
            )
        }
    }
}

@Composable
fun DailyExpensesLineChart(dailyExpenses: Map<Int, Double>, modifier: Modifier = Modifier) {
    val modelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(dailyExpenses) {
        if (dailyExpenses.isNotEmpty()) {
            val entries = dailyExpenses.entries
                .sortedBy { it.key }
                .map { (day, amount) ->
                FloatEntry(x = day.toFloat(), y = amount.toFloat())
            }
            modelProducer.setEntries(entries)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            "Daily Spending",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ProvideChartStyle(chartStyle = m3ChartStyle()) {
            Chart(
                chart = lineChart(),
                chartModelProducer = modelProducer,
                startAxis = rememberStartAxis(
                    valueFormatter = { value, _ -> "€%.0f".format(value) }
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { value, _ -> value.toInt().toString() },
                    title = "Day of Month"
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

@Composable
fun CategoryBarChart(expensesByCategory: Map<ExpenseCategory, Double>, modifier: Modifier = Modifier) {
    val categoryColors = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
        Color(0xFFFFD54F), Color(0xFFBA68C8), Color(0xFFA1887F), Color(0xFF4DD0E1)
    )

    val columnStyles = remember(categoryColors) {
        categoryColors.map { color ->
            LineComponent(
                color = color.toArgb(),
                thicknessDp = 4f,
                shape = Shapes.roundedCornerShape(topRightPercent = 25, topLeftPercent = 25)
            )
        }
    }

    val categories = expensesByCategory.keys.map { it.name }
    val modelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(expensesByCategory) {
        val multiSeriesData = expensesByCategory.values.mapIndexed { index, value ->
            listOf(FloatEntry(x = index.toFloat(), y = value.toFloat()))
        }
        modelProducer.setEntries(multiSeriesData)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            "Expenses by Category",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ProvideChartStyle(chartStyle = m3ChartStyle()) {
            Chart(
                chart = columnChart(
                    columns = columnStyles,
                    spacing = 16.dp
                ),
                chartModelProducer = modelProducer,
                startAxis = rememberStartAxis(
                    valueFormatter = { value, _ -> "€%.0f".format(value) }
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { _, _ -> "" }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Column {
            categories.chunked(2).forEach { rowCategories ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowCategories.forEach { category ->
                        val index = categories.indexOf(category)
                        val color = categoryColors[index % categoryColors.size]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color = color, shape = RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = category, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CategoryPieChart(expensesByCategory: Map<ExpenseCategory, Double>, modifier: Modifier = Modifier) {
    val categoryColors = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
        Color(0xFFFFD54F), Color(0xFFBA68C8), Color(0xFFA1887F), Color(0xFF4DD0E1)
    )

    val total = expensesByCategory.values.sum()
    val categories = expensesByCategory.keys.toList()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Expenses Distribution",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (total == 0.0) {
            Text("No expenses available.", style = MaterialTheme.typography.bodyMedium)
            return
        }

        Canvas(modifier = Modifier.size(200.dp)) {
            var startAngle = -90f
            expensesByCategory.values.forEachIndexed { index, value ->
                val sweepAngle: Float = (value / total * 360f).toFloat()
                drawArc(
                    color = categoryColors[index % categoryColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            categories.forEachIndexed { index, category ->
                val color = categoryColors[index % categoryColors.size]
                val amount = expensesByCategory[category] ?: 0.0
                val percentage = (amount / total * 100).toInt()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    Box(
                        modifier = Modifier.size(12.dp).background(color, shape = RoundedCornerShape(1.dp)),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${category.name}: €${String.format("%.2f", amount)} ($percentage%)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
