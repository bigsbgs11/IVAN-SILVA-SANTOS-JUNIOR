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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreditCardEntity
import com.example.data.TransactionEntity
import com.example.ui.theme.*
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardsScreen(
    cards: List<CreditCardEntity>,
    transactions: List<TransactionEntity>,
    onAddCard: (name: String, limit: Double, closingDay: Int, dueDay: Int, colorHex: String) -> Unit,
    onDeleteCard: (Long) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cartões de Crédito", fontWeight = FontWeight.Bold, color = Blue900) },
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
                text = { Text("Novo Cartão", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_card")
            )
        },
        modifier = Modifier.testTag("credit_cards_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (cards.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Gray400, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Nenhum cartão cadastrado", fontWeight = FontWeight.Bold, color = Gray800, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Cadastre seus cartões para controlar limite total, fatura e fechamento.",
                                fontSize = 13.sp,
                                color = Gray500,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(cards, key = { it.id }) { card ->
                    // Compute utilized limit from transactions linked to this card or labeled Crédito
                    val usedLimit = transactions
                        .filter { it.type == "EXPENSE" && (it.cardId == card.id || it.title.contains(card.name, ignoreCase = true)) && Formatters.isThisMonth(it.date) }
                        .sumOf { it.amount }
                    val availableLimit = (card.creditLimit - usedLimit).coerceAtLeast(0.0)
                    val usedPercentage = if (card.creditLimit > 0) ((usedLimit / card.creditLimit) * 100.0).coerceIn(0.0, 100.0) else 0.0

                    // Premium visual credit card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("credit_card_item_${card.id}"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Blue900),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Blue900, Blue800, Blue700)
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = card.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteCard(card.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir cartão", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Limite Utilizado e Disponível
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Limite Utilizado", fontSize = 11.sp, color = Blue100.copy(alpha = 0.8f))
                                    Text(Formatters.formatCurrency(usedLimit), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Limite Disponível", fontSize = 11.sp, color = Blue100.copy(alpha = 0.8f))
                                    Text(Formatters.formatCurrency(availableLimit), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth((usedPercentage / 100f).toFloat().coerceAtLeast(0.01f))
                                        .clip(CircleShape)
                                        .background(if (usedPercentage > 85) ExpenseRed else Blue200)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Dates: Fechamento & Vencimento
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Fecha dia ${card.closingDay}",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Vence dia ${card.dueDay}",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Total: ${Formatters.formatCurrency(card.creditLimit)}",
                                    fontSize = 12.sp,
                                    color = Blue100
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Credit Card Dialog
    if (showAddDialog) {
        var cardName by remember { mutableStateOf("") }
        var limitText by remember { mutableStateOf("") }
        var closingDayText by remember { mutableStateOf("") }
        var dueDayText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Cadastrar Cartão de Crédito", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("Nome do cartão *") },
                        placeholder = { Text("Ex: Nubank, Inter, Visa Infinite...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_card_name")
                    )

                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it },
                        label = { Text("Limite Total (R$) *") },
                        placeholder = { Text("Ex: 5.000,00") },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold, color = Blue700) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_card_limit")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = closingDayText,
                            onValueChange = { closingDayText = it },
                            label = { Text("Fechamento") },
                            placeholder = { Text("Dia (ex: 20)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_card_closing_day")
                        )

                        OutlinedTextField(
                            value = dueDayText,
                            onValueChange = { dueDayText = it },
                            label = { Text("Vencimento") },
                            placeholder = { Text("Dia (ex: 27)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_card_due_day")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = Formatters.parseCurrencyInput(limitText)
                        val closing = closingDayText.toIntOrNull() ?: 20
                        val due = dueDayText.toIntOrNull() ?: 27
                        if (cardName.isNotBlank() && limit > 0) {
                            onAddCard(cardName.trim(), limit, closing.coerceIn(1, 31), due.coerceIn(1, 31), "#1E3A8A")
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
