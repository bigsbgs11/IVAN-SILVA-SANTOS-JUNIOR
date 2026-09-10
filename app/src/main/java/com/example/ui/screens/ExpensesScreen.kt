package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinancialConstants
import com.example.data.TransactionEntity
import com.example.ui.components.TransactionItemRow
import com.example.ui.theme.*
import com.example.util.Formatters

enum class PeriodFilter(val label: String) {
    THIS_MONTH("Este mês"),
    TODAY("Hoje"),
    THIS_WEEK("Esta semana"),
    LAST_MONTH("Mês anterior"),
    ALL("Todos")
}

enum class SortOrder(val label: String) {
    DATE_DESC("Mais recentes"),
    DATE_ASC("Mais antigas"),
    AMOUNT_DESC("Maior valor"),
    AMOUNT_ASC("Menor valor")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    expenses: List<TransactionEntity>,
    customCategories: List<String>,
    onAddExpenseClick: () -> Unit,
    onEditExpense: (TransactionEntity) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onAddNewCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(PeriodFilter.THIS_MONTH) }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var selectedPaymentMethod by remember { mutableStateOf("Todas") }
    var sortOrder by remember { mutableStateOf(SortOrder.DATE_DESC) }
    var showCategoryFilterMenu by remember { mutableStateOf(false) }
    var showPaymentFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    val allCategories = remember(customCategories) {
        listOf("Todas") + (FinancialConstants.DEFAULT_EXPENSE_CATEGORIES + customCategories).distinct()
    }

    val allPaymentMethods = remember {
        listOf("Todas") + FinancialConstants.PAYMENT_METHODS
    }

    // Filter logic
    val filteredExpenses = remember(
        expenses,
        searchQuery,
        selectedPeriod,
        selectedCategory,
        selectedPaymentMethod,
        sortOrder
    ) {
        var list = expenses.filter { it.type == "EXPENSE" }

        // Period filter
        list = when (selectedPeriod) {
            PeriodFilter.TODAY -> list.filter { Formatters.isToday(it.date) }
            PeriodFilter.THIS_WEEK -> list.filter { Formatters.isThisWeek(it.date) }
            PeriodFilter.THIS_MONTH -> list.filter { Formatters.isThisMonth(it.date) }
            PeriodFilter.LAST_MONTH -> list.filter { Formatters.isPreviousMonth(it.date) }
            PeriodFilter.ALL -> list
        }

        // Category filter
        if (selectedCategory != "Todas") {
            list = list.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }

        // Payment method filter
        if (selectedPaymentMethod != "Todas") {
            list = list.filter { it.paymentMethod.equals(selectedPaymentMethod, ignoreCase = true) }
        }

        // Search query
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.note.contains(searchQuery, ignoreCase = true)
            }
        }

        // Sort order
        when (sortOrder) {
            SortOrder.DATE_DESC -> list.sortedByDescending { it.date }
            SortOrder.DATE_ASC -> list.sortedBy { it.date }
            SortOrder.AMOUNT_DESC -> list.sortedByDescending { it.amount }
            SortOrder.AMOUNT_ASC -> list.sortedBy { it.amount }
        }
    }

    val totalSpent = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.amount }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("expenses_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = ExpenseRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo Gasto", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("fab_add_expense")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Banner: Total Spent
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ExpenseRedLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Quanto você gastou?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Gray600
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Total de Despesas (${selectedPeriod.label})",
                            fontSize = 14.sp,
                            color = Gray800
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatCurrency(totalSpent),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = ExpenseRed,
                            modifier = Modifier.testTag("text_total_expenses")
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar gastos...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Gray500) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar", tint = Gray500)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_expenses")
                )
            }

            // Period Filter Horizontal Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PeriodFilter.values().forEach { period ->
                        val isSelected = selectedPeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPeriod = period },
                            label = { Text(period.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Blue600,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Dropdowns: Category, Payment Method, Sort
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Dropdown
                    Box {
                        AssistChip(
                            onClick = { showCategoryFilterMenu = true },
                            label = { Text("Cat: $selectedCategory") },
                            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = showCategoryFilterMenu,
                            onDismissRequest = { showCategoryFilterMenu = false }
                        ) {
                            allCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        showCategoryFilterMenu = false
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("+ Criar Categoria", color = Blue600, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showCategoryFilterMenu = false
                                    showNewCategoryDialog = true
                                }
                            )
                        }
                    }

                    // Payment Method Dropdown
                    Box {
                        AssistChip(
                            onClick = { showPaymentFilterMenu = true },
                            label = { Text("Pagamento: $selectedPaymentMethod") },
                            leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = showPaymentFilterMenu,
                            onDismissRequest = { showPaymentFilterMenu = false }
                        ) {
                            allPaymentMethods.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = {
                                        selectedPaymentMethod = method
                                        showPaymentFilterMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Sort Order
                    Box {
                        AssistChip(
                            onClick = { showSortMenu = true },
                            label = { Text(sortOrder.label) },
                            leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = {
                                        sortOrder = order
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Expenses List
            if (filteredExpenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterListOff,
                                contentDescription = null,
                                tint = Gray400,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nenhuma despesa encontrada",
                                fontWeight = FontWeight.Bold,
                                color = Gray700,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tente ajustar os filtros ou adicione uma nova despesa.",
                                color = Gray500,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredExpenses, key = { it.id }) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        onClick = { onEditExpense(tx) },
                        onDelete = { onDeleteExpense(tx.id) }
                    )
                }
            }
        }
    }

    // New Category Dialog
    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("Criar Nova Categoria", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCategoryInput,
                    onValueChange = { newCategoryInput = it },
                    label = { Text("Nome da Categoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryInput.isNotBlank()) {
                            onAddNewCategory(newCategoryInput.trim())
                            selectedCategory = newCategoryInput.trim()
                            newCategoryInput = ""
                            showNewCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Criar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) {
                    Text("Cancelar", color = Gray600)
                }
            }
        )
    }
}
