package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CreditCardEntity
import com.example.data.TransactionEntity
import com.example.ui.CategoryBudgetStatus
import com.example.ui.FinancialSummary
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    summary: FinancialSummary,
    recentTransactions: List<TransactionEntity>,
    budgetAlerts: List<CategoryBudgetStatus>,
    creditCards: List<CreditCardEntity>,
    onAddTransactionClick: () -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Overview Financial Card
        item {
            DashboardOverviewCard(
                summary = summary,
                onAddClick = onAddTransactionClick
            )
        }

        // 2. Budget Alert Banners (if any exceeded or near limit)
        val alertItem = budgetAlerts.firstOrNull { it.isExceeded || it.isNearLimit }
        if (alertItem != null) {
            item {
                val message = if (alertItem.isExceeded) {
                    "Você ultrapassou o limite planejado para ${alertItem.category}."
                } else {
                    "Atenção: você está perto do limite em ${alertItem.category} (${alertItem.percentage.toInt()}%)."
                }
                BudgetAlertBanner(message = message)
            }
        }

        // 3. Visual Comparison Chart: Receitas x Despesas x Saldo
        item {
            FinancialComparisonChartCard(
                income = summary.monthIncome,
                expense = summary.monthExpense,
                balance = summary.currentBalance
            )
        }

        // 4. Section Header: Movimentações Recentes
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Movimentações Recentes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                )

                TextButton(
                    onClick = onViewAllTransactionsClick,
                    modifier = Modifier.testTag("btn_view_all_transactions")
                ) {
                    Text(
                        text = "Ver todas",
                        color = Blue600,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 5. Recent Transactions List
        if (recentTransactions.isEmpty()) {
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
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nenhuma movimentação registrada",
                            fontWeight = FontWeight.SemiBold,
                            color = Gray700,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toque no botão acima para adicionar seu primeiro gasto ou receita.",
                            color = Gray500,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentTransactions.take(8), key = { it.id }) { tx ->
                TransactionItemRow(
                    transaction = tx,
                    onClick = { onEditTransaction(tx) },
                    onDelete = { onDeleteTransaction(tx.id) }
                )
            }
        }
    }
}
