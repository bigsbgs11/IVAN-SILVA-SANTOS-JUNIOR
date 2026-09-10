package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreditCardEntity
import com.example.data.FinancialConstants
import com.example.data.TransactionEntity
import com.example.ui.theme.*
import com.example.util.Formatters

@Composable
fun BgsHeader(
    title: String = "BGS NA LINHA",
    subtitle: String? = null,
    trailingAction: @Composable (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Blue700, Blue500)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BGS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue900,
                            letterSpacing = 0.5.sp
                        )
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray500
                            )
                        )
                    }
                }
            }
            if (trailingAction != null) {
                trailingAction()
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val isIncome = transaction.type == "INCOME"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isIncome) IncomeGreenLight else ExpenseRedLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(transaction.category, isIncome),
                    contentDescription = transaction.category,
                    tint = if (isIncome) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Gray900,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Gray500,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = " • ",
                        color = Gray400,
                        fontSize = 12.sp
                    )
                    Text(
                        text = transaction.paymentMethod,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Blue600,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                    if (transaction.isFixed) {
                        Text(
                            text = " • Fixa",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray500,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                Text(
                    text = Formatters.formatDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Gray400,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount & Type
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+ " else "- "}${Formatters.formatCurrency(transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = Gray400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Excluir", color = ExpenseRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(category: String, isIncome: Boolean): ImageVector {
    return if (isIncome) {
        when (category.lowercase()) {
            "salário", "salario" -> Icons.Default.AccountBalance
            "vendas" -> Icons.Default.Store
            "freelance" -> Icons.Default.LaptopMac
            "comissões", "comissoes" -> Icons.Default.Paid
            "investimentos" -> Icons.Default.TrendingUp
            "renda extra" -> Icons.Default.AttachMoney
            else -> Icons.Default.ArrowUpward
        }
    } else {
        when (category.lowercase()) {
            "alimentação", "alimentacao" -> Icons.Default.Restaurant
            "moradia" -> Icons.Default.Home
            "transporte" -> Icons.Default.DirectionsCar
            "saúde", "saude" -> Icons.Default.LocalHospital
            "educação", "educacao" -> Icons.Default.School
            "lazer" -> Icons.Default.SportsEsports
            "compras" -> Icons.Default.ShoppingBag
            "contas" -> Icons.Default.ReceiptLong
            "assinaturas" -> Icons.Default.Subscriptions
            "cartão", "cartao" -> Icons.Default.CreditCard
            else -> Icons.Default.ArrowDownward
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    initialType: String = "EXPENSE",
    existingTransaction: TransactionEntity? = null,
    creditCards: List<CreditCardEntity> = emptyList(),
    customCategories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: Long,
        paymentMethod: String,
        note: String,
        isFixed: Boolean,
        cardId: Long?
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(existingTransaction?.type ?: initialType) }
    var title by remember { mutableStateOf(existingTransaction?.title ?: "") }
    var amountText by remember { mutableStateOf(if (existingTransaction != null) String.format("%.2f", existingTransaction.amount).replace(".", ",") else "") }
    var selectedCategory by remember {
        mutableStateOf(
            existingTransaction?.category ?: if (selectedType == "INCOME") FinancialConstants.DEFAULT_INCOME_CATEGORIES.first() else FinancialConstants.DEFAULT_EXPENSE_CATEGORIES.first()
        )
    }
    var selectedPaymentMethod by remember { mutableStateOf(existingTransaction?.paymentMethod ?: "Pix") }
    var note by remember { mutableStateOf(existingTransaction?.note ?: "") }
    var isFixed by remember { mutableStateOf(existingTransaction?.isFixed ?: false) }
    var selectedCardId by remember { mutableStateOf(existingTransaction?.cardId) }
    var isCustomCategoryMode by remember { mutableStateOf(false) }
    var newCustomCategoryText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categoryList = remember(selectedType, customCategories) {
        val base = if (selectedType == "INCOME") FinancialConstants.DEFAULT_INCOME_CATEGORIES else FinancialConstants.DEFAULT_EXPENSE_CATEGORIES
        (base + customCategories).distinct()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingTransaction == null) {
                    if (selectedType == "INCOME") "Adicionar Receita" else "Adicionar Gasto"
                } else "Editar Lançamento",
                fontWeight = FontWeight.Bold,
                color = Blue900
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gray100)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isExpense = selectedType == "EXPENSE"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExpense) ExpenseRed else Color.Transparent)
                            .clickable {
                                selectedType = "EXPENSE"
                                selectedCategory = FinancialConstants.DEFAULT_EXPENSE_CATEGORIES.first()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Despesa",
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) Color.White else Gray700,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isExpense) IncomeGreen else Color.Transparent)
                            .clickable {
                                selectedType = "INCOME"
                                selectedCategory = FinancialConstants.DEFAULT_INCOME_CATEGORIES.first()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Receita",
                            fontWeight = FontWeight.Bold,
                            color = if (!isExpense) Color.White else Gray700,
                            fontSize = 14.sp
                        )
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor (R$) *") },
                    placeholder = { Text("0,00") },
                    prefix = { Text("R$ ", fontWeight = FontWeight.Bold, color = Blue700) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_transaction_amount"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Description
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Descrição *") },
                    placeholder = { Text("Ex: Supermercado, Aluguel, Salário...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_transaction_title"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category selector
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categoryList.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Payment Method (only for expenses or both)
                var paymentExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = !paymentExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPaymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Forma de Pagamento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = paymentExpanded,
                        onDismissRequest = { paymentExpanded = false }
                    ) {
                        FinancialConstants.PAYMENT_METHODS.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    selectedPaymentMethod = method
                                    paymentExpanded = false
                                }
                            )
                        }
                    }
                }

                // If Credit Card selected and credit cards exist
                if (selectedPaymentMethod == "Crédito" && creditCards.isNotEmpty()) {
                    var cardExpanded by remember { mutableStateOf(false) }
                    val currentCardName = creditCards.find { it.id == selectedCardId }?.name ?: "Selecionar Cartão"
                    ExposedDropdownMenuBox(
                        expanded = cardExpanded,
                        onExpandedChange = { cardExpanded = !cardExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentCardName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vincular ao Cartão") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = cardExpanded,
                            onDismissRequest = { cardExpanded = false }
                        ) {
                            creditCards.forEach { card ->
                                DropdownMenuItem(
                                    text = { Text("${card.name} (Venc: dia ${card.dueDay})") },
                                    onClick = {
                                        selectedCardId = card.id
                                        cardExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Fixed / Variable switch (for expenses)
                if (selectedType == "EXPENSE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isFixed) "Despesa Fixa" else "Despesa Variável",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray700
                        )
                        Switch(
                            checked = isFixed,
                            onCheckedChange = { isFixed = it }
                        )
                    }
                }

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Observação (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = Formatters.parseCurrencyInput(amountText)
                    if (title.isBlank()) {
                        errorMessage = "Digite a descrição da movimentação."
                        return@Button
                    }
                    if (amount <= 0) {
                        errorMessage = "O valor deve ser maior que zero."
                        return@Button
                    }
                    onSave(
                        title.trim(),
                        amount,
                        selectedType,
                        selectedCategory,
                        existingTransaction?.date ?: System.currentTimeMillis(),
                        selectedPaymentMethod,
                        note.trim(),
                        isFixed,
                        if (selectedPaymentMethod == "Crédito") selectedCardId else null
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                modifier = Modifier.testTag("btn_save_transaction")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Gray600)
            }
        }
    )
}
