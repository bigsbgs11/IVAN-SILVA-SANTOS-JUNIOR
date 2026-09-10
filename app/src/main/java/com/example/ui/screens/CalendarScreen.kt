package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.components.TransactionItemRow
import com.example.ui.theme.*
import com.example.util.Formatters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    transactions: List<TransactionEntity>,
    onBack: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    var calendarMonth by remember {
        mutableStateOf(Calendar.getInstance())
    }

    var selectedDateMillis by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    val daysInMonth = remember(calendarMonth) {
        val cal = calendarMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Pair(firstDayOfWeek, maxDays)
    }

    // Daily summary for selected date
    val selectedDayTransactions = remember(transactions, selectedDateMillis) {
        transactions.filter { Formatters.isSameDay(it.date, selectedDateMillis) }
    }

    val dayIncome = remember(selectedDayTransactions) {
        selectedDayTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val dayExpense = remember(selectedDayTransactions) {
        selectedDayTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val dayBalance = dayIncome - dayExpense

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendário Financeiro", fontWeight = FontWeight.Bold, color = Blue900) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Blue900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = Modifier.testTag("calendar_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Switcher Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                val next = calendarMonth.clone() as Calendar
                                next.add(Calendar.MONTH, -1)
                                calendarMonth = next
                            }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior", tint = Blue700)
                            }

                            Text(
                                text = Formatters.formatMonthYear(calendarMonth.timeInMillis),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Blue900
                            )

                            IconButton(onClick = {
                                val next = calendarMonth.clone() as Calendar
                                next.add(Calendar.MONTH, 1)
                                calendarMonth = next
                            }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês", tint = Blue700)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Weekdays header
                        val weekdays = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
                        Row(modifier = Modifier.fillMaxWidth()) {
                            weekdays.forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray500
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Days Grid
                        val (firstOffset, totalDays) = daysInMonth
                        val totalCells = firstOffset + totalDays
                        val rows = (totalCells + 6) / 7

                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                for (c in 0 until 7) {
                                    val cellIndex = r * 7 + c
                                    val dayNumber = cellIndex - firstOffset + 1

                                    if (dayNumber in 1..totalDays) {
                                        val calForDay = calendarMonth.clone() as Calendar
                                        calForDay.set(Calendar.DAY_OF_MONTH, dayNumber)
                                        val dayMillis = calForDay.timeInMillis
                                        val isSelected = Formatters.isSameDay(dayMillis, selectedDateMillis)

                                        val hasIncome = transactions.any { it.type == "INCOME" && Formatters.isSameDay(it.date, dayMillis) }
                                        val hasExpense = transactions.any { it.type == "EXPENSE" && Formatters.isSameDay(it.date, dayMillis) }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Blue600 else Color.Transparent)
                                                .clickable { selectedDateMillis = dayMillis },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else Gray800
                                                )
                                                // Indicator dots
                                                if (hasIncome || hasExpense) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        if (hasIncome) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else IncomeGreen)
                                                            )
                                                        }
                                                        if (hasExpense) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else ExpenseRed)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Card: Receitas daquele dia, Despesas daquele dia, Saldo do dia
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = Formatters.formatDateHeader(selectedDateMillis),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Blue900
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Receitas do dia", fontSize = 12.sp, color = Gray500)
                                Text(
                                    Formatters.formatCurrency(dayIncome),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                            Column {
                                Text("Despesas do dia", fontSize = 12.sp, color = Gray500)
                                Text(
                                    Formatters.formatCurrency(dayExpense),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Saldo do dia", fontSize = 12.sp, color = Gray500)
                                Text(
                                    Formatters.formatCurrency(dayBalance),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dayBalance >= 0) Blue600 else ExpenseRed
                                )
                            }
                        }
                    }
                }
            }

            // List of transactions on selected day
            item {
                Text(
                    text = "Lançamentos neste dia (${selectedDayTransactions.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Gray900,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (selectedDayTransactions.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma movimentação registrada nesta data.",
                        fontSize = 13.sp,
                        color = Gray500,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(selectedDayTransactions, key = { it.id }) { tx ->
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
