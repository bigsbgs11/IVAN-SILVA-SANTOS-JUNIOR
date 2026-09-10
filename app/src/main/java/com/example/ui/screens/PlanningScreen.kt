package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryBudgetEntity
import com.example.data.FinancialConstants
import com.example.ui.CategoryBudgetStatus
import com.example.ui.FinancialSummary
import com.example.ui.theme.*
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    summary: FinancialSummary,
    monthlyIncome: Double,
    savingsGoal: Double,
    categoryBudgets: List<CategoryBudgetStatus>,
    onUpdateIncomeAndSavings: (income: Double, savings: Double) -> Unit,
    onSaveCategoryLimit: (category: String, limit: Double) -> Unit,
    onDeleteBudget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var tempIncomeText by remember { mutableStateOf(if (monthlyIncome > 0) String.format("%.2f", monthlyIncome).replace(".", ",") else "") }
    var tempSavingsText by remember { mutableStateOf(if (savingsGoal > 0) String.format("%.2f", savingsGoal).replace(".", ",") else "") }

    var showAddCategoryLimitDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(FinancialConstants.DEFAULT_EXPENSE_CATEGORIES.first()) }
    var categoryLimitText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("planning_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Orçamento Mensal
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Orçamento Mensal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Blue900
                                )
                            )
                            Text(
                                text = "Controle o que entra e o que você planeja poupar",
                                style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                            )
                        }

                        IconButton(
                            onClick = {
                                tempIncomeText = if (monthlyIncome > 0) String.format("%.2f", monthlyIncome).replace(".", ",") else ""
                                tempSavingsText = if (savingsGoal > 0) String.format("%.2f", savingsGoal).replace(".", ",") else ""
                                showEditBudgetDialog = true
                            },
                            modifier = Modifier.testTag("btn_edit_monthly_budget")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar Orçamento", tint = Blue600)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Automatic Calculations Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BudgetCalcBox(
                            title = "Renda mensal",
                            value = Formatters.formatCurrency(monthlyIncome),
                            color = Blue600,
                            modifier = Modifier.weight(1f)
                        )
                        BudgetCalcBox(
                            title = "Gastos atuais",
                            value = Formatters.formatCurrency(summary.monthExpense),
                            color = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BudgetCalcBox(
                            title = "Economia planejada",
                            value = Formatters.formatCurrency(savingsGoal),
                            color = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        BudgetCalcBox(
                            title = "Disponível para gastar",
                            value = Formatters.formatCurrency(summary.availableToSpend),
                            color = IncomeGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Indicador: Meta de Economia
                    Text(
                        text = "Meta de Economia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Meta: ${Formatters.formatCurrency(savingsGoal)}",
                            fontSize = 12.sp,
                            color = Gray600
                        )
                        Text(
                            text = "Economizado: ${Formatters.formatCurrency(summary.totalSaved)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Blue600
                        )
                        Text(
                            text = "Progresso: ${summary.savingsProgress.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.savingsProgress >= 100) IncomeGreen else Blue700
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val progressRatio = (summary.savingsProgress / 100.0).toFloat().coerceIn(0f, 1f)
                    val animProgress by animateFloatAsState(
                        targetValue = progressRatio,
                        animationSpec = tween(700),
                        label = "savings_progress"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(Gray100)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animProgress.coerceAtLeast(0.01f))
                                .clip(CircleShape)
                                .background(if (summary.savingsProgress >= 100) IncomeGreen else Blue500)
                        )
                    }
                }
            }
        }

        // Section: Limites por Categoria
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Limites por Categoria",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                    )
                    Text(
                        text = "Acompanhe seus gastos por categoria",
                        style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                    )
                }

                Button(
                    onClick = { showAddCategoryLimitDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_add_category_limit")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Definir Limite", fontSize = 13.sp)
                }
            }
        }

        if (categoryBudgets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nenhum limite de categoria definido",
                            fontWeight = FontWeight.Bold,
                            color = Gray700
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Defina tetos de gastos para Alimentação, Transporte, Lazer, etc.",
                            fontSize = 13.sp,
                            color = Gray500,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(categoryBudgets, key = { it.category }) { budget ->
                CategoryBudgetCard(
                    budget = budget,
                    onDelete = { onDeleteBudget(budget.category) }
                )
            }
        }
    }

    // Dialog: Edit Monthly Budget
    if (showEditBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showEditBudgetDialog = false },
            title = { Text("Definir Orçamento Mensal", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Atualize sua renda e a meta de quanto quer poupar a cada mês.",
                        fontSize = 13.sp,
                        color = Gray600
                    )

                    OutlinedTextField(
                        value = tempIncomeText,
                        onValueChange = { tempIncomeText = it },
                        label = { Text("Quanto ganho por mês? *") },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold, color = Blue700) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempSavingsText,
                        onValueChange = { tempSavingsText = it },
                        label = { Text("Quanto quero economizar?") },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold, color = Blue700) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val inc = Formatters.parseCurrencyInput(tempIncomeText)
                        val sav = Formatters.parseCurrencyInput(tempSavingsText)
                        onUpdateIncomeAndSavings(inc, sav)
                        showEditBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBudgetDialog = false }) {
                    Text("Cancelar", color = Gray600)
                }
            }
        )
    }

    // Dialog: Add Category Limit
    if (showAddCategoryLimitDialog) {
        var categoryExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddCategoryLimitDialog = false },
            title = { Text("Definir Limite de Categoria", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                            FinancialConstants.DEFAULT_EXPENSE_CATEGORIES.forEach { cat ->
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

                    OutlinedTextField(
                        value = categoryLimitText,
                        onValueChange = { categoryLimitText = it },
                        label = { Text("Limite Mensal (R$) *") },
                        placeholder = { Text("Ex: 800,00") },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold, color = Blue700) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_category_limit")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = Formatters.parseCurrencyInput(categoryLimitText)
                        if (limit > 0) {
                            onSaveCategoryLimit(selectedCategory, limit)
                            categoryLimitText = ""
                            showAddCategoryLimitDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Definir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryLimitDialog = false }) {
                    Text("Cancelar", color = Gray600)
                }
            }
        )
    }
}

@Composable
private fun BudgetCalcBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Gray50)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Gray500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    budget: CategoryBudgetStatus,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_card_${budget.category}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.category,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Gray900
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remover limite",
                        tint = Gray400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub info: Limite | Gasto | Disponível
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Limite", fontSize = 11.sp, color = Gray500)
                    Text(text = Formatters.formatCurrency(budget.limit), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Gray800)
                }
                Column {
                    Text(text = "Gasto", fontSize = 11.sp, color = Gray500)
                    Text(text = Formatters.formatCurrency(budget.spent), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ExpenseRed)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Disponível", fontSize = 11.sp, color = Gray500)
                    Text(
                        text = Formatters.formatCurrency(budget.remaining),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (budget.remaining >= 0) IncomeGreen else ExpenseRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            val ratio = (budget.percentage / 100.0).toFloat().coerceIn(0f, 1f)
            val barColor = when {
                budget.isExceeded -> ExpenseRed
                budget.isNearLimit -> WarningAmber
                else -> IncomeGreen
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
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

            // Exceeded or Near Limit Warning Message
            if (budget.isExceeded) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Você ultrapassou o limite planejado para esta categoria.",
                        color = ExpenseRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (budget.isNearLimit) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Atenção: você está perto do limite (${budget.percentage.toInt()}%).",
                        color = WarningAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
