package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.TransactionEntity
import com.example.ui.theme.*
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    transactions: List<TransactionEntity>,
    onBack: () -> Unit
) {
    // Current month transactions
    val monthTransactions = remember(transactions) {
        transactions.filter { Formatters.isThisMonth(it.date) }
    }

    val totalExpense = remember(monthTransactions) {
        monthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val totalIncome = remember(monthTransactions) {
        monthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }

    // Expenses by Category
    val expensesByCategory = remember(monthTransactions) {
        monthTransactions.filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Incomes by Category
    val incomesByCategory = remember(monthTransactions) {
        monthTransactions.filter { it.type == "INCOME" }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Expenses by Payment Method
    val expensesByPayment = remember(monthTransactions) {
        monthTransactions.filter { it.type == "EXPENSE" }
            .groupBy { it.paymentMethod }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatórios Financeiros", fontWeight = FontWeight.Bold, color = Blue900) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Blue900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = Modifier.testTag("reports_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Comparison Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Comparação Este Mês",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Blue900
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Recebido", fontSize = 12.sp, color = Gray500)
                                Text(
                                    Formatters.formatCurrency(totalIncome),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Gasto", fontSize = 12.sp, color = Gray500)
                                Text(
                                    Formatters.formatCurrency(totalExpense),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        val net = totalIncome - totalExpense
                        Text(
                            text = "Saldo do Período: ${Formatters.formatCurrency(net)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (net >= 0) Blue600 else ExpenseRed
                        )
                    }
                }
            }

            // Gastos por Categoria
            item {
                ReportSectionCard(
                    title = "Gastos por Categoria",
                    subtitle = "Onde seu dinheiro está sendo aplicado",
                    items = expensesByCategory,
                    total = totalExpense,
                    barColor = ExpenseRed
                )
            }

            // Receitas por Categoria
            item {
                ReportSectionCard(
                    title = "Receitas por Categoria",
                    subtitle = "Origem das suas entradas financeiras",
                    items = incomesByCategory,
                    total = totalIncome,
                    barColor = IncomeGreen
                )
            }

            // Gastos por Forma de Pagamento
            item {
                ReportSectionCard(
                    title = "Gastos por Forma de Pagamento",
                    subtitle = "Distribuição entre Pix, Crédito, Débito, etc.",
                    items = expensesByPayment,
                    total = totalExpense,
                    barColor = Blue600
                )
            }
        }
    }
}

@Composable
private fun ReportSectionCard(
    title: String,
    subtitle: String,
    items: List<Pair<String, Double>>,
    total: Double,
    barColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Blue900)
            Text(text = subtitle, fontSize = 12.sp, color = Gray500)
            Spacer(modifier = Modifier.height(14.dp))

            if (items.isEmpty()) {
                Text(
                    text = "Nenhum dado registrado para este mês.",
                    fontSize = 13.sp,
                    color = Gray500,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            } else {
                items.forEach { (name, amount) ->
                    val percentage = if (total > 0) (amount / total) * 100.0 else 0.0
                    val ratio = (percentage / 100.0).toFloat().coerceIn(0f, 1f)

                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Gray800)
                            Row {
                                Text(
                                    text = Formatters.formatCurrency(amount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gray900
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${percentage.toInt()}%)",
                                    fontSize = 12.sp,
                                    color = Gray500
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Gray100)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio.coerceAtLeast(0.01f))
                                    .clip(CircleShape)
                                    .background(barColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
