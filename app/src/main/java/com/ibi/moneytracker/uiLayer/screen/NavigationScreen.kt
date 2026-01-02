package com.ibi.moneytracker.uiLayer.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ibi.moneytracker.MoneyTrackerApplication
import com.ibi.moneytracker.R
import com.ibi.moneytracker.uiLayer.viewmodel.AddEditViewModel
import com.ibi.moneytracker.uiLayer.viewmodel.ChartViewViewModel
import com.ibi.moneytracker.uiLayer.viewmodel.DashboardViewModel
import com.ibi.moneytracker.uiLayer.viewmodel.ExpensesViewModel
import com.ibi.moneytracker.uiLayer.viewmodel.ViewModelFactory

enum class AppTab(val title: Int, val icon: ImageVector) {
    Dashboard(
        title = R.string.dashboard, icon = Icons.Default.Dashboard
    ),
    Add(
        title = R.string.add, icon = Icons.Default.Add
    ),
    ChartView(
        title = R.string.chartView, icon = Icons.Default.AddChart
    ),
    Edit(
        title = R.string.edit, icon = Icons.Default.Edit
    ),
    Expense(
        title = R.string.expense, icon = Icons.Default.Preview
    );
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyTrackerAppBar(currentScreen: AppTab, canNavigateBack: Boolean, navigateUp: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        })
}

@Composable
fun BottomNavigationBar(currentScreen: AppTab, onTabSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier) {
        val navigationItems = listOf(AppTab.Dashboard, AppTab.Add, AppTab.Expense, AppTab.ChartView)

        navigationItems.forEach { screen ->
            NavigationBarItem(
                selected = currentScreen == screen,
                onClick = { onTabSelected(screen.name) },
                label = { Text(stringResource(id = screen.title)) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = stringResource(id = screen.title)
                    )
                })
        }
    }
}

@Composable
fun NavigationScreen(
    navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = AppTab.valueOf(
        backStackEntry?.destination?.route?.replaceAfter("/", "")?.replace("/", "")
            ?: AppTab.Dashboard.name
    )

    val app = LocalContext.current.applicationContext as MoneyTrackerApplication
    val repository = remember { app.repository }

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = ViewModelFactory(repository)
    )
    val addEditViewModel: AddEditViewModel = viewModel(
        factory = ViewModelFactory(repository)
    )
    val expensesViewModel: ExpensesViewModel = viewModel(
        factory = ViewModelFactory(repository)
    )
    val chartViewViewModel: ChartViewViewModel = viewModel(
        factory = ViewModelFactory(repository)
    )


    Scaffold(topBar = {
        MoneyTrackerAppBar(
            currentScreen = currentScreen,
            canNavigateBack = navController.previousBackStackEntry != null,
            navigateUp = { navController.navigateUp() }
        )
    }, bottomBar = {
        BottomNavigationBar(
            currentScreen = currentScreen, onTabSelected = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            })
    }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppTab.Dashboard.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = AppTab.Dashboard.name) {
                DashboardScreen(
                    viewModel = dashboardViewModel
                )
            }

            composable(route = AppTab.Add.name) {
                AddScreen(
                    onNextButtonClicked = { navController.popBackStack() },
                    navController = navController,
                    viewModel = addEditViewModel
                )
            }

            composable(route = AppTab.ChartView.name) {
                ChartView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium)), viewModel = chartViewViewModel
                )
            }
            val editRoute = "${AppTab.Edit.name}/{expenseId}"
            composable(
                route = editRoute, arguments = listOf(navArgument("expenseId") {
                    type = NavType.LongType
                })
            ) { backStackEntry ->
                val expenseIdLong = backStackEntry.arguments?.getLong("expenseId")
                val expenseId = expenseIdLong?.toInt()

                if (expenseId != null) {
                    EditScreen(
                        expenseId = expenseId,
                        viewModel = addEditViewModel,
                        navController = navController,
                    )
                } else {
                    navController.popBackStack()
                }
            }
            composable(route = AppTab.Expense.name) {
                ExpensesScreen(
                    navController = navController, viewModel = expensesViewModel
                )
            }
        }
    }
}
