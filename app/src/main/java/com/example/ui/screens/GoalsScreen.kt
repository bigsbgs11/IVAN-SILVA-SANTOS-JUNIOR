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
import com.example.data.GoalEntity
import com.example.ui.theme.*
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    onAddGoal: (name: String, targetAmount: Double, initialSaved: Double, deadline: String) -> Unit,
    onAddSavedAmount: (GoalEntity, Double) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<GoalEntity?>(null) }
    var depositAmountText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metas Financeiras", fontWeight = FontWeight.Bold, color = Blue900) },
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
                text = { Text("Nova Meta", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_goal")
            )
        },
        modifier = Modifier.testTag("goals_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (goals.isEmpty()) {
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
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Gray400, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Nenhuma meta definida", fontWeight = FontWeight.Bold, color = Gray800, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Defina objetivos como celular novo, viagem, reserva de emergência ou carro.",
                                fontSize = 13.sp,
                                color = Gray500,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    val percentage = if (goal.targetAmount > 0) ((goal.savedAmount / goal.targetAmount) * 100.0).coerceIn(0.0, 100.0) else 0.0
                    val remaining = (goal.targetAmount - goal.savedAmount).coerceAtLeast(0.0)
                    val animRatio by animateFloatAsState(
                        targetValue = (percentage / 100.0).toFloat(),
                        animationSpec = tween(600),
                        label = "goal_anim"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_item_${goal.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Blue50),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Flag, contentDescription = null, tint = Blue600, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = goal.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gray900)
                                        if (goal.deadline.isNotBlank()) {
                                            Text(text = "Prazo: ${goal.deadline}", fontSize = 12.sp, color = Gray500)
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteGoal(goal.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir meta", tint = Gray400, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Values Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Guardado", fontSize = 11.sp, color = Gray500)
                                    Text(Formatters.formatCurrency(goal.savedAmount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Objetivo", fontSize = 11.sp, color = Gray500)
                                    Text(Formatters.formatCurrency(goal.targetAmount), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray800)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Falta", fontSize = 11.sp, color = Gray500)
                                    Text(Formatters.formatCurrency(remaining), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Blue600)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Progress bar
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
                                        .fillMaxWidth(animRatio.coerceAtLeast(0.01f))
                                        .clip(CircleShape)
                                        .background(if (percentage >= 100) IncomeGreen else Blue600)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${percentage.toInt()}% concluído",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (percentage >= 100) IncomeGreen else Blue700
                                )

                                TextButton(
                                    onClick = {
                                        selectedGoalForDeposit = goal
                                        depositAmountText = ""
                                    }
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Guardar valor", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Goal Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("") }
        var savedText by remember { mutableStateOf("") }
        var deadline by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nova Meta Financeira", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Exemplos:", fontSize = 12.sp, color = Gray500)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Comprar celular", "Viagem", "Reserva").forEach { sample ->
                            SuggestionChip(
                                onClick = { name = sample },
                                label = { Text(sample, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome da meta *") },
                        placeholder = { Text("Ex: Celular novo, Carro...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_goal_name")
                    )

                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Valor alvo (R$) *") },
                        placeholder = { Text("Ex: 3.000,00") },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold, color = Blue700) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_goal_target")
                    )

                    OutlinedTextField(
                        value = savedText,
                        onValueChange = { savedText = it },
                        label = { Text("Valor já guardado") },
                        placeholder = { Text("Ex: 500,00") },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold, color = Blue700) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_goal_saved")
                    )

                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Prazo desejado (opcional)") },
                        placeholder = { Text("Ex: Dezembro / 2026") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = Formatters.parseCurrencyInput(targetText)
                        val initial = Formatters.parseCurrencyInput(savedText)
                        if (name.isNotBlank() && target > 0) {
                            onAddGoal(name.trim(), target, initial, deadline.trim())
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Criar Meta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = Gray600)
                }
            }
        )
    }

    // Deposit to Goal Dialog
    if (selectedGoalForDeposit != null) {
        val targetGoal = selectedGoalForDeposit!!
        AlertDialog(
            onDismissRequest = { selectedGoalForDeposit = null },
            title = { Text("Guardar em: ${targetGoal.name}", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Quanto você deseja adicionar a esta meta hoje?",
                        fontSize = 13.sp,
                        color = Gray600
                    )
                    OutlinedTextField(
                        value = depositAmountText,
                        onValueChange = { depositAmountText = it },
                        label = { Text("Valor a guardar *") },
                        placeholder = { Text("0,00") },
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
                        val amount = Formatters.parseCurrencyInput(depositAmountText)
                        if (amount > 0) {
                            onAddSavedAmount(targetGoal, amount)
                            selectedGoalForDeposit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGoalForDeposit = null }) {
                    Text("Cancelar", color = Gray600)
                }
            }
        )
    }
}
