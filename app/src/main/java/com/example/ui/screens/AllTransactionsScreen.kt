package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.components.TransactionItemRow
import com.example.ui.theme.*
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    transactions: List<TransactionEntity>,
    onBack: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("TODAS") } // TODAS, EXPENSE, INCOME
    var selectedPeriod by remember { mutableStateOf(PeriodFilter.ALL) }
    var sortOrder by remember { mutableStateOf(SortOrder.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredTransactions = remember(
        transactions,
        searchQuery,
        selectedTypeFilter,
        selectedPeriod,
        sortOrder
    ) {
        var list = transactions

        // Type filter
        if (selectedTypeFilter != "TODAS") {
            list = list.filter { it.type == selectedTypeFilter }
        }

        // Period filter
        list = when (selectedPeriod) {
            PeriodFilter.TODAY -> list.filter { Formatters.isToday(it.date) }
            PeriodFilter.THIS_WEEK -> list.filter { Formatters.isThisWeek(it.date) }
            PeriodFilter.THIS_MONTH -> list.filter { Formatters.isThisMonth(it.date) }
            PeriodFilter.LAST_MONTH -> list.filter { Formatters.isPreviousMonth(it.date) }
            PeriodFilter.ALL -> list
        }

        // Search query
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true) ||
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todas as Movimentações", fontWeight = FontWeight.Bold, color = Blue900) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Blue900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = Modifier.testTag("all_transactions_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar por nome ou categoria...") },
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
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Type Filter & Sort Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeFilter == "TODAS",
                        onClick = { selectedTypeFilter = "TODAS" },
                        label = { Text("Todas") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Blue600, selectedLabelColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    )
                    FilterChip(
                        selected = selectedTypeFilter == "EXPENSE",
                        onClick = { selectedTypeFilter = "EXPENSE" },
                        label = { Text("Despesas") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ExpenseRed, selectedLabelColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    )
                    FilterChip(
                        selected = selectedTypeFilter == "INCOME",
                        onClick = { selectedTypeFilter = "INCOME" },
                        label = { Text("Receitas") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = IncomeGreen, selectedLabelColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Sort order button
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

            // Results count
            item {
                Text(
                    text = "${filteredTransactions.size} movimentações encontradas",
                    fontSize = 12.sp,
                    color = Gray500,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (filteredTransactions.isEmpty()) {
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
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Gray400, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Nenhum lançamento encontrado", fontWeight = FontWeight.Bold, color = Gray700)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ajuste os filtros ou adicione uma nova movimentação.", fontSize = 13.sp, color = Gray500)
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        onClick = { onEditTransaction(tx) },
                        onDelete = { onDeleteTransaction(tx.id) }
                    )
                }
            }
        }
    }
}
