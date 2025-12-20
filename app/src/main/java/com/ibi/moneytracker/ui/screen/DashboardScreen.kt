package com.ibi.moneytracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ibi.moneytracker.ui.viewmodel.DashboardViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel,
) {
    val totalMonthlyYearlyIncludedCost by viewModel.totalMonthlyYearlyIncludedCost.collectAsStateWithLifecycle()
    val totalMonthlyCost by viewModel.totalMonthlyCost.collectAsStateWithLifecycle()
    val totalOneTimeCost by viewModel.totalOneTimeCost.collectAsStateWithLifecycle() // Assuming you add this to the VM

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            // --- 1. PRIMARY BALANCE CARD (Top Focus) ---
            PrimaryBalanceCard(
                total = totalMonthlyCost,
                title = "Total Monthly Expenses"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. SECONDARY METRICS (Side-by-Side Row) ---
            MetricCardsRow(
                monthlyWithYearly = totalMonthlyYearlyIncludedCost,
                // Using a placeholder variable for a third metric.
                // You can replace this with expensesByCategory or other data later.
                oneTimeCost = totalOneTimeCost
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- NEW/MODIFIED COMPOSABLES ---

@Composable
fun PrimaryBalanceCard(total: Double, title: String) {
    val formattedTotal = String.format(Locale.getDefault(), "%.2f", total)

    Card(
        modifier = Modifier.fillMaxWidth(),
        // Modern, moderate rounding
        shape = RoundedCornerShape(16.dp),
        // Use primary color for the main focus card
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "€$formattedTotal",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun MetricCardsRow(monthlyWithYearly: Double, oneTimeCost: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Metric 1: Monthly + Yearly
        MetricCard(
            total = monthlyWithYearly,
            title = "Mth + Yr Incl.",
            modifier = Modifier.weight(1f) // Takes half the available width
        )

        // Metric 2: One-Time/Other
        MetricCard(
            total = oneTimeCost,
            title = "One-Time",
            modifier = Modifier.weight(1f) // Takes the other half
        )
    }
}


@Composable
fun MetricCard(total: Double, title: String, modifier: Modifier = Modifier) {
    val formattedTotal = String.format(Locale.getDefault(), "%.2f", total)

    Card(
        modifier = modifier, // Uses the weight modifier passed from the Row
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start // Left-align for easier scanning
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "€$formattedTotal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}