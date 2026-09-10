package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.TransactionEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AddEditTransactionDialog
import com.example.ui.components.BgsHeader
import com.example.ui.screens.*
import com.example.ui.theme.Blue600
import com.example.ui.theme.Blue900
import com.example.ui.theme.Gray400
import com.example.ui.theme.Gray500
import com.example.ui.theme.MyApplicationTheme

enum class BottomTab(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("dashboard", "Início", Icons.Default.Home, "tab_dashboard"),
    EXPENSES("expenses", "Gastos", Icons.Default.ArrowDownward, "tab_expenses"),
    INCOMES("incomes", "Receitas", Icons.Default.ArrowUpward, "tab_incomes"),
    PLANNING("planning", "Planejamento", Icons.Default.Tune, "tab_planning"),
    MORE("more", "Mais", Icons.Default.Apps, "tab_more")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BgsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BgsApp(viewModel: MainViewModel) {
    val isFirstLaunchDone by viewModel.isFirstLaunchDone.collectAsStateWithLifecycle()
    val isPinEnabled by viewModel.isPinEnabled.collectAsStateWithLifecycle()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()

    if (!isFirstLaunchDone) {
        OnboardingScreen(
            onFinish = { income, savings ->
                viewModel.completeFirstLaunch(income, savings)
            }
        )
        return
    }

    if (isPinEnabled && !isAppUnlocked) {
        LockScreen(
            onUnlockSuccess = {
                viewModel.unlockApp(viewModel.pinCode.value)
            },
            onVerifyPin = { pin ->
                viewModel.unlockApp(pin)
            }
        )
        return
    }

    // Main App Navigation
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomTab.DASHBOARD.route

    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val financialSummary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val categoryBudgets by viewModel.categoryBudgetsStatus.collectAsStateWithLifecycle()
    val creditCards by viewModel.allCreditCards.collectAsStateWithLifecycle()
    val fixedBills by viewModel.allFixedBills.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val customCategories by viewModel.allCustomCategories.collectAsStateWithLifecycle()
    val monthlyIncome by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val savingsGoal by viewModel.savingsGoal.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var initialDialogType by remember { mutableStateOf("EXPENSE") }

    val isTopLevelDestination = BottomTab.values().any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
        topBar = {
            if (isTopLevelDestination) {
                val headerSubtitle = when (currentRoute) {
                    BottomTab.DASHBOARD.route -> "Controle Financeiro Pessoal"
                    BottomTab.EXPENSES.route -> "Gestão de Saídas"
                    BottomTab.INCOMES.route -> "Gestão de Entradas"
                    BottomTab.PLANNING.route -> "Orçamento & Metas"
                    BottomTab.MORE.route -> "Mais Opções"
                    else -> null
                }
                BgsHeader(
                    title = "BGS NA LINHA",
                    subtitle = headerSubtitle,
                    trailingAction = {
                        IconButton(
                            onClick = {
                                initialDialogType = "EXPENSE"
                                transactionToEdit = null
                                showAddDialog = true
                            },
                            modifier = Modifier.testTag("btn_header_add")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Adicionar Lançamento",
                                tint = Blue600,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    BottomTab.values().forEach { tab ->
                        val isSelected = currentRoute == tab.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Blue600,
                                selectedTextColor = Blue600,
                                indicatorColor = Blue600.copy(alpha = 0.12f),
                                unselectedIconColor = Gray500,
                                unselectedTextColor = Gray500
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.DASHBOARD.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Tab 1: Dashboard
            composable(BottomTab.DASHBOARD.route) {
                DashboardScreen(
                    summary = financialSummary,
                    recentTransactions = allTransactions,
                    budgetAlerts = categoryBudgets,
                    creditCards = creditCards,
                    onAddTransactionClick = {
                        initialDialogType = "EXPENSE"
                        transactionToEdit = null
                        showAddDialog = true
                    },
                    onViewAllTransactionsClick = {
                        navController.navigate("all_transactions")
                    },
                    onEditTransaction = { tx ->
                        transactionToEdit = tx
                        initialDialogType = tx.type
                        showAddDialog = true
                    },
                    onDeleteTransaction = { id ->
                        viewModel.deleteTransaction(id)
                    }
                )
            }

            // Tab 2: Expenses
            composable(BottomTab.EXPENSES.route) {
                ExpensesScreen(
                    expenses = allTransactions,
                    customCategories = customCategories.filter { it.type == "EXPENSE" }.map { it.name },
                    onAddExpenseClick = {
                        initialDialogType = "EXPENSE"
                        transactionToEdit = null
                        showAddDialog = true
                    },
                    onEditExpense = { tx ->
                        transactionToEdit = tx
                        initialDialogType = "EXPENSE"
                        showAddDialog = true
                    },
                    onDeleteExpense = { id ->
                        viewModel.deleteTransaction(id)
                    },
                    onAddNewCategory = { name ->
                        viewModel.addCustomCategory(name, "EXPENSE")
                    }
                )
            }

            // Tab 3: Incomes
            composable(BottomTab.INCOMES.route) {
                IncomesScreen(
                    incomes = allTransactions,
                    customCategories = customCategories.filter { it.type == "INCOME" }.map { it.name },
                    onAddIncomeClick = {
                        initialDialogType = "INCOME"
                        transactionToEdit = null
                        showAddDialog = true
                    },
                    onEditIncome = { tx ->
                        transactionToEdit = tx
                        initialDialogType = "INCOME"
                        showAddDialog = true
                    },
                    onDeleteIncome = { id ->
                        viewModel.deleteTransaction(id)
                    },
                    onAddNewCategory = { name ->
                        viewModel.addCustomCategory(name, "INCOME")
                    }
                )
            }

            // Tab 4: Planning
            composable(BottomTab.PLANNING.route) {
                PlanningScreen(
                    summary = financialSummary,
                    monthlyIncome = monthlyIncome,
                    savingsGoal = savingsGoal,
                    categoryBudgets = categoryBudgets,
                    onUpdateIncomeAndSavings = { inc, sav ->
                        viewModel.setMonthlyIncome(inc)
                        viewModel.setSavingsGoal(sav)
                    },
                    onSaveCategoryLimit = { cat, limit ->
                        viewModel.saveBudget(cat, limit)
                    },
                    onDeleteBudget = { cat ->
                        val target = viewModel.allBudgets.value.find { it.categoryName == cat }
                        if (target != null) {
                            viewModel.deleteBudget(target)
                        }
                    }
                )
            }

            // Tab 5: More
            composable(BottomTab.MORE.route) {
                MoreScreen(
                    onNavigateTo = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // Sub-screen: Reports
            composable("reports") {
                ReportsScreen(
                    transactions = allTransactions,
                    onBack = { navController.popBackStack() }
                )
            }

            // Sub-screen: Calendar
            composable("calendar") {
                CalendarScreen(
                    transactions = allTransactions,
                    onBack = { navController.popBackStack() },
                    onEditTransaction = { tx ->
                        transactionToEdit = tx
                        initialDialogType = tx.type
                        showAddDialog = true
                    },
                    onDeleteTransaction = { id ->
                        viewModel.deleteTransaction(id)
                    }
                )
            }

            // Sub-screen: Fixed Bills
            composable("fixed_bills") {
                FixedBillsScreen(
                    bills = fixedBills,
                    onAddBill = { name, amount, dueDay, isMonthly, cat ->
                        viewModel.addFixedBill(name, amount, dueDay, isMonthly, cat)
                    },
                    onTogglePaid = { bill ->
                        viewModel.toggleFixedBillPaid(bill)
                    },
                    onDeleteBill = { id ->
                        viewModel.deleteFixedBill(id)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Sub-screen: Credit Cards
            composable("credit_cards") {
                CreditCardsScreen(
                    cards = creditCards,
                    transactions = allTransactions,
                    onAddCard = { name, limit, closing, due, color ->
                        viewModel.addCreditCard(name, limit, closing, due, color)
                    },
                    onDeleteCard = { id ->
                        viewModel.deleteCreditCard(id)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Sub-screen: Goals
            composable("goals") {
                GoalsScreen(
                    goals = goals,
                    onAddGoal = { name, target, initial, deadline ->
                        viewModel.addGoal(name, target, initial, deadline)
                    },
                    onAddSavedAmount = { goal, amount ->
                        viewModel.addToGoalSaved(goal, amount)
                    },
                    onDeleteGoal = { id ->
                        viewModel.deleteGoal(id)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Sub-screen: All Transactions
            composable("all_transactions") {
                AllTransactionsScreen(
                    transactions = allTransactions,
                    onBack = { navController.popBackStack() },
                    onEditTransaction = { tx ->
                        transactionToEdit = tx
                        initialDialogType = tx.type
                        showAddDialog = true
                    },
                    onDeleteTransaction = { id ->
                        viewModel.deleteTransaction(id)
                    }
                )
            }

            // Sub-screen: Settings
            composable("settings") {
                SettingsScreen(
                    isPinEnabled = isPinEnabled,
                    onSetPin = { enabled, pin ->
                        viewModel.setPin(enabled, pin)
                    },
                    transactions = allTransactions,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    // Modal Dialog: Add / Edit Transaction
    if (showAddDialog) {
        val expenseCustomCats = customCategories.filter { it.type == "EXPENSE" }.map { it.name }
        val incomeCustomCats = customCategories.filter { it.type == "INCOME" }.map { it.name }
        val combinedCustomCats = if (initialDialogType == "INCOME") incomeCustomCats else expenseCustomCats

        AddEditTransactionDialog(
            initialType = initialDialogType,
            existingTransaction = transactionToEdit,
            creditCards = creditCards,
            customCategories = combinedCustomCats,
            onDismiss = {
                showAddDialog = false
                transactionToEdit = null
            },
            onSave = { title, amount, type, category, date, paymentMethod, note, isFixed, cardId ->
                if (transactionToEdit != null) {
                    viewModel.updateTransaction(
                        transactionToEdit!!.copy(
                            title = title,
                            amount = amount,
                            type = type,
                            category = category,
                            paymentMethod = paymentMethod,
                            note = note,
                            isFixed = isFixed,
                            cardId = cardId
                        )
                    )
                } else {
                    viewModel.addTransaction(
                        title = title,
                        amount = amount,
                        type = type,
                        category = category,
                        date = date,
                        paymentMethod = paymentMethod,
                        note = note,
                        isFixed = isFixed,
                        cardId = cardId
                    )
                }
                showAddDialog = false
                transactionToEdit = null
            }
        )
    }
}
