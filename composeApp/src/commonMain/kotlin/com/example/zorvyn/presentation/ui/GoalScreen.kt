package com.example.zorvyn.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.presentation.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = { Text("Smart Savings Goal", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                GoalVisualHeader(uiState.balance)
            }

            item {
                StreakMilestoneCard(uiState.balance.streakDays)
            }

            item {
                RuleSection()
            }

            item {
                HistorySection()
            }
        }
    }
}

@Composable
fun GoalVisualHeader(balance: BalanceState) {
    val progress = (balance.savings / balance.monthlyGoal).toFloat().coerceIn(0f, 1f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF00C853),
                    strokeWidth = 14.dp,
                    trackColor = Color(0xFF2A2A2A),
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Complete",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GoalStat("Goal", "₹${balance.monthlyGoal.toInt()}")
                GoalStat("Saved", "₹${balance.savings.toInt()}")
                GoalStat("Left", "₹${(balance.monthlyGoal - balance.savings).toInt().coerceAtLeast(0)}")
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Smart feedback banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF00C853).copy(alpha = 0.1f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "🧠 ${balance.smartFeedback}",
                    color = Color(0xFF00C853),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GoalStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StreakMilestoneCard(streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD600).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD600).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔥", fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "$streak Day Saving Streak!",
                    color = Color(0xFFFFD600),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Keep going! You're building a healthy habit.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun RuleSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "How it works", color = Color.White, fontWeight = FontWeight.Bold)
        RuleItem(Icon = Icons.Default.TrendingUp, text = "Add savings or income to build streak")
        RuleItem(Icon = Icons.Default.Warning, text = "Avoid overspending to maintain habit")
        RuleItem(Icon = Icons.Default.Star, text = "Reach 100% to unlock new features!")
    }
}

@Composable
fun RuleItem(Icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icon, "Rule", modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = Color.Gray, fontSize = 13.sp)
    }
}

@Composable
fun HistorySection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "Engagement History", color = Color.White, fontWeight = FontWeight.Bold)
        repeat(6) { i ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Day ${6-i} Streak Maintenance", color = Color.White, fontSize = 14.sp)
                    Text("✅", fontSize = 14.sp)
                }
            }
        }
    }
}
