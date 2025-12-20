package com.ibi.moneytracker.ui.screen

import android.content.res.Resources
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ibi.moneytracker.data.ExpenseCategory
import com.ibi.moneytracker.ui.viewmodel.DashboardViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry

@Composable
fun ChartView(modifier: Modifier, viewModel: DashboardViewModel) {
    val expensesByCategory by viewModel.expensesByCategory.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween
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
    }

}

@Composable
fun CategoryBarChart(
    expensesByCategory: Map<ExpenseCategory, Double>,
    modifier: Modifier = Modifier
) {
    // 1. Define Colors
    val categoryColors = listOf(
        Color(0xFFE57373), // red
        Color(0xFF64B5F6), // blue
        Color(0xFF81C784), // green
        Color(0xFFFFD54F), // yellow
        Color(0xFFBA68C8), // purple
        Color(0xFFA1887F), // brown
        Color(0xFF4DD0E1), // cyan
    )

    // 2. Define the Style for each color (Vico uses LineComponent)
    val columnStyles = remember(categoryColors) {
        categoryColors.map { color ->
            LineComponent(
                color = color.toArgb(),
                thicknessDp = 2f,
                shape = Shapes.roundedCornerShape(topRightPercent = 25, topLeftPercent = 25)
            )
        }
    }

    val categories = expensesByCategory.keys.map { it.name }
    val modelProducer = remember { ChartEntryModelProducer() }

    // 3. THE TRICK: Pass a List of Lists.
    // This treats every bar as a separate "series", so Vico assigns a different color to each.
    LaunchedEffect(expensesByCategory) {
        val multiSeriesData = expensesByCategory.values.mapIndexed { index, value ->
            listOf(FloatEntry(x = index.toFloat(), y = value.toFloat()))
        }
        modelProducer.setEntries(multiSeriesData)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {

        Text(
            "Expenses by Category",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ProvideChartStyle(chartStyle = m3ChartStyle()) {
            Chart(
                // 4. Pass the list of styles. Vico will use style[0] for series[0], style[1] for series[1], etc.
                chart = columnChart(
                    columns = columnStyles,
                    spacing = 16.dp
                ),
                chartModelProducer = modelProducer,
                startAxis = rememberStartAxis(
                    valueFormatter = { value, _ -> "€%.0f".format(value) }
                ),
                // 5. Hide the Bottom Axis Labels (since we have the legend)
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { _, _ -> "" }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. LEGEND: Map the colors to the category names
        categories.forEachIndexed { index, category ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                // Safely get the color (wrap around if more categories than colors)
                val color = categoryColors[index % categoryColors.size]

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            color = color,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun CategoryPieChart(
    expensesByCategory: Map<ExpenseCategory, Double>,
    modifier: Modifier = Modifier
) {
    // Use the same colors as your bar chart (reuse or define at top-level!)
    val categoryColors = listOf(
        Color(0xFFE57373), // red
        Color(0xFF64B5F6), // blue
        Color(0xFF81C784), // green
        Color(0xFFFFD54F), // yellow
        Color(0xFFBA68C8), // purple
        Color(0xFFA1887F), // brown
        Color(0xFF4DD0E1), // cyan
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

        // Pie chart
        Canvas(
            modifier = Modifier
                .size(220.dp)
        ) {
            var startAngle = -90f

            expensesByCategory.values.forEachIndexed { index, value ->
                val sweepAngle: Float = (value / total * 360f).toFloat()

                drawArc(
                    color = Color.Black,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                )
                drawArc(
                    color = categoryColors[index % categoryColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Column(modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEachIndexed { index, category ->
                val color = categoryColors[index % categoryColors.size]
                val amount = expensesByCategory[category] ?: 0.0
                val percentage = (amount / total * 100).toInt()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 2.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(color, shape = RoundedCornerShape(1.dp)),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = " - $percentage%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
/*
@Composable
fun ComposeBarChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier,
    barColors: List<Color> = listOf(
        Color(0xFFE57373),
        Color(0xFF64B5F6),
        Color(0xFF81C784),
        Color(0xFFFFD54F),
        Color(0xFFBA68C8),
        Color(0xFFA1887F),
        Color(0xFF4DD0E1),
    )
) {
    if (data.isEmpty()) {
        Text("No data available", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val maxValue = data.values.maxOrNull() ?: 1.0
    val categories = data.keys.toList()
    val values = data.values.toList()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            "Expenses by Category",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // DRAW BAR CHART
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 10.dp)
                .border(BorderStroke(1.dp, Color.Black))
        ) {
            val barWidth = size.width / (values.size * 2f)
            val space = barWidth

            values.forEachIndexed { index, value ->
                val barHeight: Double = (value / maxValue) * size.height

                val left = index * (barWidth + space) + space
                val top = size.height - barHeight.toFloat()
                val right = left + barWidth

                drawRoundRect(
                    color = barColors[index % barColors.size],
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight.toFloat()),
                    cornerRadius = CornerRadius(20f, 20f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LEGEND
        Column {
            categories.forEachIndexed { index, category ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                color = barColors[index % barColors.size],
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(category, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
*/


@Preview
@Composable
fun Cat(){

    CategoryPieChart(
        expensesByCategory = mapOf(
            ExpenseCategory.FOOD to 100.0,
            ExpenseCategory.HOUSE to 200.0,
        ),
        modifier = Modifier
    )
}
