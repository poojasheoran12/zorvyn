package org.example.project.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.*
import org.example.project.presentation.DashboardUiState
import org.example.project.presentation.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFF00C853),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { GreetingHeader() }
            item { BalanceCard(uiState.balance) }
            item { SummaryRow(uiState.balance) }
            item { ChartSection(uiState.categoryChartData) }
            item {
                Text(
                    text = "Recent Transactions",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            items(uiState.recentTransactions) { transaction ->
                TransactionItem(transaction)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun GreetingHeader() {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val greeting = when(now.hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
    
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$greeting, Pooja 👋",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = "${now.month.name.lowercase()}, ${now.dayOfMonth} ${now.dayOfWeek.name.lowercase()}",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BalanceCard(balance: BalanceState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF00C853).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient Background Detail
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00C853).copy(alpha = 0.15f), Color.Transparent),
                        radius = 400f
                    ),
                    center = center
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹ ${balance.currentBalance.toInt()}",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Current Balance",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BalanceStatItem(
                        label = "Income",
                        amount = "₹ ${balance.income.toInt()}",
                        color = Color(0xFF00C853),
                        isIncome = true
                    )
                    BalanceStatItem(
                        label = "Expense",
                        amount = "₹ ${balance.expense.toInt()}",
                        color = Color(0xFFFF5252),
                        isIncome = false
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceStatItem(label: String, amount: String, color: Color, isIncome: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = (if(isIncome) "+" else "-") + " " + amount,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SummaryRow(balance: BalanceState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            label = "Income",
            amount = "₹${balance.income.toInt()}",
            color = Color(0xFF00C853),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Expense",
            amount = "₹${balance.expense.toInt()}",
            color = Color(0xFFFF5252),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Savings",
            amount = "₹${balance.savings.toInt()}",
            color = Color(0xFF448AFF),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(label: String, amount: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = amount, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChartSection(data: Map<String, Double>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Spending by Category",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie Chart
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    PieChart(data)
                }
                
                // Legend
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colors = listOf(Color(0xFF00C853), Color(0xFFFFD600), Color(0xFFFF5252), Color(0xFF448AFF))
                    data.keys.toList().forEachIndexed { index, category ->
                        LegendItem(category, colors[index % colors.size])
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(data: Map<String, Double>) {
    val total = data.values.sum()
    val colors = listOf(Color(0xFF00C853), Color(0xFFFFD600), Color(0xFFFF5252), Color(0xFF448AFF))
    
    val animateFloat = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animateFloat.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
        )
    }
    
    Canvas(modifier = Modifier.size(120.dp)) {
        var startAngle = -90f
        data.values.forEachIndexed { index, value ->
            val sweepAngle = (value.toFloat() / total.toFloat()) * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle * animateFloat.value,
                useCenter = false,
                style = Stroke(width = 40f, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1E1E)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = transaction.category.icon, fontSize = 20.sp)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaction.note, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(text = transaction.formattedDate, color = Color.Gray, fontSize = 12.sp)
        }
        
        Text(
            text = (if(transaction.type == TransactionType.INCOME) "+" else "-") + " ₹${transaction.amount.toInt()}",
            color = if(transaction.type == TransactionType.INCOME) Color(0xFF00C853) else Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
