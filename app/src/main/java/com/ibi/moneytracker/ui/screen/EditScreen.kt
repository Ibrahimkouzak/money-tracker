package com.ibi.moneytracker.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ibi.moneytracker.data.BillingCycle
import com.ibi.moneytracker.data.ExpenseCategory
import com.ibi.moneytracker.ui.viewmodel.DashboardViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel,
    expenseId: Int,
) {
    val expense = viewModel.getExpenseById(expenseId)
    var name by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var selectedCycle by remember { mutableStateOf(BillingCycle.MONTHLY) }
    var selectedCategory by remember { mutableStateOf(null as ExpenseCategory?) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isCycleMenuExpanded by remember { mutableStateOf(false) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(expense) {
        expense?.let {
            name = it.name
            cost = String.format(Locale.US, "%.2f", it.cost)
            selectedCycle = it.billingCycle
            selectedCategory = it.category
            selectedDate = it.firstPaymentDate
        }
    }

    val costDouble = cost.toDoubleOrNull()
    val isFormValid = name.isNotBlank() && costDouble != null && costDouble > 0 && selectedCategory != null


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Expense") },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (expense == null) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Expense Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Cost") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = isCycleMenuExpanded,
                    onExpandedChange = { isCycleMenuExpanded = !isCycleMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCycle.name.lowercase().replaceFirstChar { it.titlecase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Cycle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCycleMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isCycleMenuExpanded,
                        onDismissRequest = { isCycleMenuExpanded = false }
                    ) {
                        BillingCycle.entries.forEach { cycle ->
                            DropdownMenuItem(
                                text = { Text(cycle.name.lowercase().replaceFirstChar { it.titlecase() }) },
                                onClick = {
                                    selectedCycle = cycle
                                    isCycleMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = isCategoryMenuExpanded,
                    onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name?.lowercase()?.replaceFirstChar { it.titlecase() } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryMenuExpanded,
                        onDismissRequest = { isCategoryMenuExpanded = false }
                    ) {
                        ExpenseCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name.lowercase().replaceFirstChar { it.titlecase() }) },
                                onClick = {
                                    selectedCategory = category
                                    isCategoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                val friendlyFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                Box {
                    OutlinedTextField(
                        value = selectedDate.format(friendlyFormatter),
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Date of Payment:") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .semantics {
                                role = Role.Button
                                contentDescription =
                                    "Date of Payment: ${selectedDate.format(friendlyFormatter)}. Click to open date picker."
                            }
                            .clickable(onClickLabel = "Open date picker") {
                                showDatePicker = true
                            }
                    )
                }
                Button(
                    onClick = {
                        expense.let {
                            val updatedExpense = it.copy(
                                name = name.trim(),
                                cost = costDouble!!,
                                billingCycle = selectedCycle,
                                category = selectedCategory!!,
                                firstPaymentDate = selectedDate
                            )
                            viewModel.updateExpense(updatedExpense)
                        }
                        navController.popBackStack()
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SAVE CHANGES")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = LocalDate.ofEpochDay(millis / (1000 * 60 * 60 * 24))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
