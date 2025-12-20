package com.ibi.moneytracker.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ibi.moneytracker.data.BillingCycle
import com.ibi.moneytracker.data.Expense
import com.ibi.moneytracker.ui.viewmodel.DashboardViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel
) {
    val expenses by viewModel.expensesList.collectAsStateWithLifecycle()
    val currentCycleFilter by viewModel.currentCycleFilter.collectAsStateWithLifecycle()
    val currentCategoryFilter by viewModel.currentCategoryFilter.collectAsStateWithLifecycle()
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    // val allExpensesList by remember { viewModel.allExpensesList.collectAsStateWithLifecycle() }
    val allCategories = remember(expenses) { expenses.map { it.category.displayName }.distinct() }


    Scaffold(
        topBar = {
            TopAppBar(
                // 2. Update the title to show both active filters
                title = {
                    // FIX 2: Ensure the Column is directly inside the title's Composable lambda
                    Column {

                        Text(
                            "Expenses view",
                            // Keeping the title small for the SmallTopAppBar
                            style = MaterialTheme.typography.titleSmall,
                            minLines = 1
                        )
                        Text(
                            "Filter by category: $currentCategoryFilter & Billing Cycle: $currentCycleFilter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            minLines = 1
                        )
                    }
                },
                actions = {

                    // --- CATEGORY FILTER DROP-DOWN (New) ---
                    Box {
                        IconButton(onClick = { isCategoryMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Filter by Category"
                            )
                        }
                        // Dropdown Menu that appears when the button is clicked
                        DropdownMenu(
                            expanded = isCategoryMenuExpanded,
                            onDismissRequest = { isCategoryMenuExpanded = false }
                        ) {
                            // "All" option (resets category filter)
                            DropdownMenuItem(
                                text = { Text("All Categories") },
                                onClick = {
                                    viewModel.setCategoryFilter(null)
                                    isCategoryMenuExpanded = false
                                }
                            )

                            // Dynamic Category options
                            allCategories.forEach { categoryName ->
                                DropdownMenuItem(
                                    text = { Text(categoryName) },
                                    onClick = {
                                        viewModel.setCategoryFilter(categoryName)
                                        isCategoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // --- CYCLE FILTER BUTTON (Existing) ---
                    IconButton(onClick = viewModel:: cycleFilter) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter by Cycle"
                        )
                    }
//                    Text("Category filter")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Log.d("ExpensesScreen", "Expenses: $expenses")
            if (expenses.isEmpty()) {
                Text(
                    text = "No expenses match the current filter criteria.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onEditClicked = { navController.navigate("${AppTab.Edit.name}/${expense.id}") },
                            onDeleteClicked = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ExpenseItem(expense: Expense,
                onEditClicked: () -> Unit,
                onDeleteClicked: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = expense.category.name, // <-- Display Category Name here
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val formattedCost = String.format(Locale.getDefault(), "%.2f", expense.cost)
            val cycleText = when (expense.billingCycle) {
                BillingCycle.WEEKLY -> "/weekly"
                BillingCycle.MONTHLY -> "/monthly"
                BillingCycle.YEARLY -> "/yearly"
                BillingCycle.OneTimePayment -> "/one time"
            }

            Text(
                text = "€$formattedCost $cycleText",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center).padding(bottom = 16.dp)
            )

            Text(
                text = "${expense.firstPaymentDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
                .padding(top = 32.dp)
            )

            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEditClicked()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDeleteClicked()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ExpensesScreenPreview(){
}
