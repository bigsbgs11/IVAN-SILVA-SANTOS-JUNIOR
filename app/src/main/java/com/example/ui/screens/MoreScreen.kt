package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class MoreHubItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun MoreScreen(
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        MoreHubItem(
            title = "Relatórios",
            subtitle = "Gráficos de despesas, receitas e formas de pagamento",
            icon = Icons.Default.BarChart,
            color = Blue600,
            route = "reports"
        ),
        MoreHubItem(
            title = "Calendário Financeiro",
            subtitle = "Visualização dia a dia de entradas e saídas",
            icon = Icons.Default.CalendarMonth,
            color = Blue500,
            route = "calendar"
        ),
        MoreHubItem(
            title = "Contas Fixas",
            subtitle = "Aluguel, água, luz, internet e streaming",
            icon = Icons.Default.ReceiptLong,
            color = WarningAmber,
            route = "fixed_bills"
        ),
        MoreHubItem(
            title = "Cartões de Crédito",
            subtitle = "Limites, fechamento de fatura e vencimentos",
            icon = Icons.Default.CreditCard,
            color = Blue700,
            route = "credit_cards"
        ),
        MoreHubItem(
            title = "Minhas Metas",
            subtitle = "Reserva, viagens, compras e objetivos",
            icon = Icons.Default.Savings,
            color = IncomeGreen,
            route = "goals"
        ),
        MoreHubItem(
            title = "Todas as Movimentações",
            subtitle = "Histórico completo com filtros e busca",
            icon = Icons.Default.History,
            color = Gray700,
            route = "all_transactions"
        ),
        MoreHubItem(
            title = "Configurações",
            subtitle = "PIN, biometria, exportação CSV e sobre",
            icon = Icons.Default.Settings,
            color = Gray600,
            route = "settings"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("more_screen")
    ) {
        Text(
            text = "Ferramentas & Gestão",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Blue900
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateTo(item.route) }
                        .testTag("hub_item_${item.route}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(item.color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = item.color,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Gray900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                fontSize = 12.sp,
                                color = Gray500
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
