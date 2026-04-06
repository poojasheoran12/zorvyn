package com.example.zorvyn.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.presentation.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = { Text("Financial Insights", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (uiState.categoryChartData.isEmpty()) {
            EmptyState(
                message = "Not enough data yet for insights. Start adding transactions to see patterns!",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    InsightHeroCard(uiState.balance)
                }

                item {
                    Text(
                        text = "Spending Patterns",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    CategoryBreakdownDetailed(uiState.categoryChartData)
                }

                item {
                    SpendingTrendCard()
                }

                item {
                    TopSpendingCard(uiState.categoryChartData)
                }
            }
        }
    }
}

@Composable
fun InsightHeroCard(balance: BalanceState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Savings Rate", color = Color.Gray, fontSize = 14.sp)
                val savingsRate = if (balance.income > 0) (balance.savings / balance.income * 100).toInt() else 0
                Text("${savingsRate}%", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Text("of income saved", color = Color.Gray, fontSize = 12.sp)
            }
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (balance.savings / balance.income).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.size(80.dp),
                    color = Color(0xFF00C853),
                    strokeWidth = 8.dp,
                    trackColor = Color.DarkGray,
                    strokeCap = StrokeCap.Round,
                )
                Icon(Icons.Default.TrendingUp, "Trend", tint = Color(0xFF00C853), modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun CategoryBreakdownDetailed(data: Map<String, Double>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val total = data.values.sum()
        data.entries.sortedByDescending { it.value }.forEach { entry ->
            CategoryProgressItem(
                label = entry.key,
                amount = entry.value,
                percentage = if (total > 0) (entry.value / total).toFloat() else 0f
            )
        }
    }
}

@Composable
fun CategoryProgressItem(label: String, amount: Double, percentage: Float) {
    val color = when(label) {
        "FOOD" -> Color(0xFF00C853)
        "TRAVEL" -> Color(0xFFFFD600)
        "SHOPPING" -> Color(0xFFFF5252)
        else -> Color(0xFF448AFF)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("₹${amount.toInt()}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color(0xFF2A2A2A),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun SpendingTrendCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Weekly Trend", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.3f, 0.8f)
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                
                heights.forEachIndexed { index, h ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight(h)
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                .background(if(index == 3) Color(0xFF00C853) else Color(0xFF2A2A2A))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(days[index], color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}


@Composable
fun TopSpendingCard(data: Map<String, Double>) {
    val top = data.entries.maxByOrNull { it.value }
    if (top != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00C853).copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, "Tip", tint = Color(0xFF00C853))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "You spent most on ${top.key} this month. Consider setting a budget to save more!",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}
