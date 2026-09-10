package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinancialConstants
import com.example.data.FixedBillEntity
import com.example.ui.theme.*
import com.example.util.Formatters
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedBillsScreen(
    bills: List<FixedBillEntity>,
    onAddBill: (name: String, amount: Double, dueDay: Int, isMonthly: Boolean, category: String) -> Unit,
    onTogglePaid: (FixedBillEntity) -> Unit,
    onDeleteBill: (Long) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val currentDay = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }

    val totalBillsAmount = remember(bills) { bills.sumOf { it.amount } }
    val totalPendingAmount = remember(bills) { bills.filter { !it.isPaid }.sumOf { it.amount } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contas Fixas", fontWeight = FontWeight.Bold, color = Blue900) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Blue900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Blue600,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nova Conta Fixa", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_fixed_bill")
            )
        },
        modifier = Modifier.testTag("fixed_bills_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total de Contas Fixas", fontSize = 12.sp, color = Gray500)
                            Text(
                                Formatters.formatCurrency(totalBillsAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue900
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pendente no Mês", fontSize = 12.sp, color = Gray500)
                            Text(
                                Formatters.formatCurrency(totalPendingAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPendingAmount > 0) ExpenseRed else IncomeGreen
                            )
                        }
                    }
                }
            }

            if (bills.isEmpty()) {
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
                            Text("Nenhuma conta fixa cadastrada", fontWeight = FontWeight.Bold, color = Gray700)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Cadastre água, luz, aluguel, internet ou streaming.", fontSize = 13.sp, color = Gray500)
                        }
                    }
                }
            } else {
                items(bills, key = { it.id }) { bill ->
                    val daysUntilDue = bill.dueDay - currentDay
                    val isDueSoon = !bill.isPaid && daysUntilDue in 0..5
                    val isOverdue = !bill.isPaid && daysUntilDue < 0

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("bill_item_${bill.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox to mark as paid
                            Checkbox(
                                checked = bill.isPaid,
                                onCheckedChange = { onTogglePaid(bill) },
                                colors = CheckboxDefaults.colors(checkedColor = IncomeGreen)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bill.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (bill.isPaid) Gray400 else Gray900
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Vence todo dia ${bill.dueDay}",
                                        fontSize = 12.sp,
                                        color = Gray500
                                    )
                                    if (isDueSoon) {
                                        Text(
                                            text = " • Vence em breve!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarningAmber
                                        )
                                    } else if (isOverdue) {
                                        Text(
                                            text = " • Vencida!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatCurrency(bill.amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (bill.isPaid) Gray400 else Blue900
                                )

                                IconButton(
                                    onClick = { onDeleteBill(bill.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir conta", tint = Gray400, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Fixed Bill Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var dueDayText by remember { mutableStateOf("") }
        var isMonthly by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Cadastrar Conta Fixa", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Quick suggestions
                    Text("Sugestões rápidas:", fontSize = 12.sp, color = Gray500)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Aluguel", "Energia", "Água", "Internet").forEach { suggestion ->
                            SuggestionChip(
                                onClick = { name = suggestion },
                                label = { Text(suggestion, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome da conta *") },
                        placeholder = { Text("Ex: Internet, Luz, Aluguel...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_bill_name")
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Valor (R$) *") },
                        placeholder = { Text("Ex: 120,00") },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold, color = Blue700) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_bill_amount")
                    )

                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { dueDayText = it },
                        label = { Text("Dia do vencimento (1 a 31) *") },
                        placeholder = { Text("Ex: 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_bill_due_day")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = Formatters.parseCurrencyInput(amountText)
                        val dueDay = dueDayText.toIntOrNull() ?: 1
                        if (name.isNotBlank() && amount > 0) {
                            onAddBill(name.trim(), amount, dueDay.coerceIn(1, 31), isMonthly, "Contas")
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Cadastrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = Gray600)
                }
            }
        )
    }
}
