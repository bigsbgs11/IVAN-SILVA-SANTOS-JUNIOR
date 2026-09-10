package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.theme.*
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isPinEnabled: Boolean,
    onSetPin: (enabled: Boolean, pin: String) -> Unit,
    transactions: List<TransactionEntity>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.Bold, color = Blue900) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Blue900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = Modifier.testTag("settings_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Segurança
            item {
                Text(
                    text = "SEGURANÇA E PRIVACIDADE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Gray500,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        // PIN Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Blue50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Blue600, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("Bloqueio por PIN / Biometria", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Gray900)
                                    Text("Solicitar senha ao abrir o app", fontSize = 12.sp, color = Gray500)
                                }
                            }
                            Switch(
                                checked = isPinEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showPinDialog = true
                                    } else {
                                        onSetPin(false, "")
                                        Toast.makeText(context, "Proteção por PIN desativada.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        if (isPinEnabled) {
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPinDialog = true }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Alterar código PIN de 4 dígitos", fontSize = 14.sp, color = Blue600, fontWeight = FontWeight.Medium)
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Gray400, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Section: Dados e Exportação
            item {
                Text(
                    text = "DADOS E EXPORTAÇÃO",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Gray500,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        // Export CSV
                        SettingsRowItem(
                            icon = Icons.Default.FileDownload,
                            title = "Exportar dados (CSV)",
                            subtitle = "Gere um arquivo com todo o histórico para planilhas",
                            onClick = {
                                exportDataToCsv(context, transactions)
                            }
                        )

                        Divider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Moeda Padrão
                        SettingsRowItem(
                            icon = Icons.Default.CurrencyExchange,
                            title = "Moeda Padrão",
                            subtitle = "Real brasileiro (R$)",
                            onClick = {
                                Toast.makeText(context, "Moeda padrão configurada como Real Brasileiro (R$).", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Section: Aplicativo
            item {
                Text(
                    text = "SOBRE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Gray500,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "Sobre o BGS NA LINHA",
                        subtitle = "Versão 1.0.0 • Controle Financeiro Pessoal",
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }
    }

    // Set/Change PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinInput = ""
                pinError = null
            },
            title = { Text("Definir PIN de 4 Dígitos", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Digite um código de 4 números para proteger suas finanças.", fontSize = 13.sp, color = Gray600)
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pinInput = it
                                pinError = null
                            }
                        },
                        label = { Text("Código PIN (4 dígitos)") },
                        placeholder = { Text("Ex: 1234") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Text(pinError!!, color = ExpenseRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            onSetPin(true, pinInput)
                            showPinDialog = false
                            pinInput = ""
                            Toast.makeText(context, "PIN ativado com sucesso!", Toast.LENGTH_SHORT).show()
                        } else {
                            pinError = "O PIN deve conter exatamente 4 dígitos."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Salvar PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
                    pinInput = ""
                    pinError = null
                }) {
                    Text("Cancelar", color = Gray600)
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("BGS NA LINHA", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Aplicativo de controle financeiro pessoal completo, 100% funcional, seguro e sem complicações.",
                        fontSize = 14.sp,
                        color = Gray700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Gestão de Despesas e Receitas", fontSize = 13.sp, color = Gray600)
                    Text(text = "• Orçamento mensal e limites por categoria", fontSize = 13.sp, color = Gray600)
                    Text(text = "• Calendário financeiro dia a dia", fontSize = 13.sp, color = Gray600)
                    Text(text = "• Gestão de contas fixas e cartões de crédito", fontSize = 13.sp, color = Gray600)
                    Text(text = "• Metas financeiras com acompanhamento de progresso", fontSize = 13.sp, color = Gray600)
                    Text(text = "• Exportação de relatórios em formato CSV", fontSize = 13.sp, color = Gray600)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Versão 1.0.0 (Produção)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue700)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Blue50),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Blue600, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Gray900)
            Text(subtitle, fontSize = 12.sp, color = Gray500)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Gray400, modifier = Modifier.size(14.dp))
    }
}

private fun exportDataToCsv(context: Context, transactions: List<TransactionEntity>) {
    Formatters.exportTransactionsCsv(context, transactions)
}
