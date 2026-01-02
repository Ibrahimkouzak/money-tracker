package com.ibi.moneytracker.uiLayer.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ibi.moneytracker.R
import com.ibi.moneytracker.uiLayer.data.BillingCycle
import com.ibi.moneytracker.uiLayer.data.ExpenseCategory
import com.ibi.moneytracker.uiLayer.screen.sharedUI.SelectOptionDropdown
import com.ibi.moneytracker.uiLayer.viewmodel.AddEditViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    navController: NavHostController,
    onNextButtonClicked: () -> Unit,
    viewModel: AddEditViewModel,
) {
    var name by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var selectedCycle by remember { mutableStateOf(BillingCycle.MONTHLY) }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val costDouble = cost.toDoubleOrNull()
    val isFormValid = name.isNotBlank() && costDouble != null && costDouble > 0 && selectedCategory != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_new_expense_title)) },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.expense_name_label)) },
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

            // --- IMPROVEMENT: Using the reusable dropdown composable ---
            SelectOptionDropdown(
                labelText = "Billing cycle",
                options = BillingCycle.entries,
                selectedOption = selectedCycle,
                onOptionSelected = { selectedCycle = it },
                optionToString = { it.name.lowercase().replaceFirstChar { char -> char.titlecase() } }
            )

            SelectOptionDropdown(
                labelText = "Category",
                options = ExpenseCategory.entries,
                selectedOption = selectedCategory,
                onOptionSelected = { selectedCategory = it },
                optionToString = { it.displayName.lowercase().replaceFirstChar { char -> char.titlecase() } }
            )

            val friendlyFormatter = DateTimeFormatter.ofPattern("MMM, dd, yyyy")

            Box {
                OutlinedTextField(
                    value = selectedDate.format(friendlyFormatter),
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Date of payment") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date")
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
                        }
                        .clickable(onClickLabel = "Open date picker") {
                            showDatePicker = true
                        }
                )
            }
            Button(
                onClick = {
                    viewModel.insertExpense(
                        name = name.trim(),
                        cost = costDouble!!,
                        billingCycle = selectedCycle,
                        category = selectedCategory!!,
                        firstPaymentDate = selectedDate
                    )
                    navController.popBackStack()
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SAVE EXPENSE")
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
                ) { Text("Ok") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}