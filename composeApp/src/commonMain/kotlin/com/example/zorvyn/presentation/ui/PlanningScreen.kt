package com.example.zorvyn.presentation.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zorvyn.domain.model.Budget
import com.example.zorvyn.domain.model.Goal
import com.example.zorvyn.presentation.BudgetViewModel
import com.example.zorvyn.presentation.GoalViewModel
import com.example.zorvyn.presentation.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    budgetViewModel: BudgetViewModel = koinViewModel(),
    goalViewModel: GoalViewModel = koinViewModel(),
    dashboardViewModel: DashboardViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val budgetUiState by budgetViewModel.uiState.collectAsState()
    val goalUiState by goalViewModel.uiState.collectAsState()
    val dashboardUiState by dashboardViewModel.uiState.collectAsState()
    
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = { Text("Financial Planning", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Action Section (New UI)
            item {
                PlanningActionSection(
                    onAddBudget = { showAddBudgetDialog = true },
                    onAddGoal = { showAddGoalDialog = true }
                )
            }

            // Overview Section
            item {
                PlanningOverviewCard(budgetUiState.streak, dashboardUiState.balance.savings)
            }

            // Budgets Section
            item {
                SectionHeader("My Budgets", Icons.Default.AccountBalanceWallet)
            }

            if (budgetUiState.isLoading) {
                item { LoadingState(modifier = Modifier.height(100.dp)) }
            } else if (budgetUiState.budgets.isEmpty()) {
                item {
                    EmptyState(
                        message = "No active budgets. Create one to track daily limits!",
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            } else {
                items(budgetUiState.budgets.size) { index ->
                    val budget = budgetUiState.budgets[index]
                    BudgetCardInPlanning(budget, onDelete = { budgetViewModel.deleteBudget(budget.id) })
                }
            }

            // Goals Section
            item {
                SectionHeader("Savings Goals", Icons.Default.Flag)
            }

            if (goalUiState.goals.isEmpty()) {
                item {
                    EmptyState(
                        message = "No goals set. Plan for your future needs!",
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            } else {
                items(goalUiState.goals.size) { index ->
                    val goal = goalUiState.goals[index]
                    PersonalGoalCardInPlanning(goal, onDelete = { goalViewModel.deleteGoal(goal.id) })
                }
            }
        }

        if (showAddBudgetDialog) {
            AddBudgetDialog(
                onDismiss = { showAddBudgetDialog = false },
                onAdd = { name, total, daily, days ->
                    budgetViewModel.addBudget(name, total, daily, days)
                    showAddBudgetDialog = false
                }
            )
        }

        if (showAddGoalDialog) {
            AddGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onAdd = { name, target, saved, date, icon ->
                    goalViewModel.addGoal(name, target, saved, date, icon)
                    showAddGoalDialog = false
                }
            )
        }
    }
}

@Composable
fun PlanningActionSection(onAddBudget: () -> Unit, onAddGoal: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PlanningActionCard(
            title = "New Budget",
            subtitle = "Track daily limits",
            icon = Icons.Default.AddChart,
            color = Color(0xFF00C853),
            onClick = onAddBudget,
            modifier = Modifier.weight(1f)
        )
        PlanningActionCard(
            title = "New Goal",
            subtitle = "Save for future",
            icon = Icons.Default.TrackChanges,
            color = Color(0xFF448AFF),
            onClick = onAddGoal,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun PlanningOverviewCard(streak: Int, totalSaved: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Savings Progress", color = Color.Gray, fontSize = 12.sp)
                Text("₹ ${totalSaved.toInt()}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFD600).copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("$streak Days", color = Color(0xFFFFD600), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BudgetCardInPlanning(budget: Budget, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = budget.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = "Ends: ${budget.endDate.toLocalDateTime(TimeZone.currentSystemDefault()).run { "$dayOfMonth ${month.name.take(3)}" }}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Gray.copy(alpha = 0.5f))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetInfoItem("Daily Limit", "₹${budget.dailyLimit.toInt()}")
                BudgetInfoItem("Total Budget", "₹${budget.totalBudget.toInt()}")
                BudgetInfoItem("Status", if(budget.isActive) "Active" else "Expired")
            }
        }
    }
}

@Composable
fun BudgetInfoItem(label: String, value: String) {
    Column {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PersonalGoalCardInPlanning(goal: Goal, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(goal.icon, fontSize = 26.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = goal.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(
                            text = "Target: ${goal.desiredDate.toLocalDateTime(TimeZone.currentSystemDefault()).run { "$dayOfMonth ${month.name.take(3)}" }}",
                            color = if (goal.isNearDeadline) Color(0xFFFF5252) else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Gray.copy(alpha = 0.5f))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            val progress = goal.progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "₹${goal.savedAmount.toInt()} saved", color = Color(0xFF00C853), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "${(progress * 100).toInt()}%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = Color(0xFF00C853),
                trackColor = Color(0xFF2A2A2A)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var daily by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Budget", color = Color.White, fontWeight = FontWeight.Bold) },
        containerColor = Color(0xFF1E1E1E),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Budget Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00C853),
                        focusedLabelColor = Color(0xFF00C853)
                    )
                )
                OutlinedTextField(
                    value = total,
                    onValueChange = { total = it },
                    label = { Text("Total Budget (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00C853),
                        focusedLabelColor = Color(0xFF00C853)
                    )
                )
                OutlinedTextField(
                    value = daily,
                    onValueChange = { daily = it },
                    label = { Text("Daily Spending Limit (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00C853),
                        focusedLabelColor = Color(0xFF00C853)
                    )
                )
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("Duration (Number of Days)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00C853),
                        focusedLabelColor = Color(0xFF00C853)
                    )
                )
                
                if (days.toIntOrNull() != null) {
                    val endDate = Clock.System.now().plus(kotlin.time.Duration.parse("${(days.toIntOrNull() ?: 0) * 24}h"))
                    Text(
                        text = "Valid Until: ${endDate.toLocalDateTime(TimeZone.currentSystemDefault()).run { "$dayOfMonth ${month.name.take(3)} $year" }}",
                        color = Color(0xFF00C853),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        name,
                        total.toDoubleOrNull() ?: 0.0,
                        daily.toDoubleOrNull() ?: 0.0,
                        days.toIntOrNull() ?: 7
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Plan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, Double, Instant, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf("") }
    var daysToAchieve by remember { mutableStateOf("30") }
    var icon by remember { mutableStateOf("🚗") }
    
    val icons = listOf("🚗", "🏠", "🏥", "🎓", "✈️", "💰")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set New Saving Goal", color = Color.White, fontWeight = FontWeight.Bold) },
        containerColor = Color(0xFF1E1E1E),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00C853),
                        focusedLabelColor = Color(0xFF00C853)
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text("Target (₹)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00C853),
                            focusedLabelColor = Color(0xFF00C853)
                        )
                    )
                    OutlinedTextField(
                        value = saved,
                        onValueChange = { saved = it },
                        label = { Text("Saved (₹)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00C853),
                            focusedLabelColor = Color(0xFF00C853)
                        )
                    )
                }
                OutlinedTextField(
                    value = daysToAchieve,
                    onValueChange = { daysToAchieve = it },
                    label = { Text("Days to Achieve") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00C853),
                        focusedLabelColor = Color(0xFF00C853)
                    )
                )
                
                if (daysToAchieve.toLongOrNull() != null) {
                    val targetDate = Clock.System.now().plus(kotlin.time.Duration.parse("${(daysToAchieve.toLongOrNull() ?: 0) * 24}h"))
                    Text(
                        text = "Target Date: ${targetDate.toLocalDateTime(TimeZone.currentSystemDefault()).run { "$dayOfMonth ${month.name.take(3)} $year" }}",
                        color = Color(0xFF00C853),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text("Select Icon", color = Color.Gray, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    icons.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (icon == emoji) Color(0xFF00C853) else Color(0xFF2A2A2A))
                                .clickable { icon = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysToAchieve.toLongOrNull() ?: 30
                    onAdd(
                        name,
                        target.toDoubleOrNull() ?: 0.0,
                        saved.toDoubleOrNull() ?: 0.0,
                        Clock.System.now().plus(kotlin.time.Duration.parse("${days * 24}h")),
                        icon
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
