package com.example.zorvyn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zorvyn.di.appModule
import org.jetbrains.compose.resources.painterResource

import com.example.zorvyn.presentation.ui.DashboardScreen
import com.example.zorvyn.presentation.ui.TransactionScreen
import com.example.zorvyn.presentation.ui.InsightsScreen
import com.example.zorvyn.presentation.ui.GoalScreen
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinAppDeclaration

@Composable
@Preview
fun App(koinAppDeclaration: KoinAppDeclaration? = null) {
    KoinApplication(application = {
        koinAppDeclaration?.invoke(this)
        modules(appModule)
    }) {
        MaterialTheme {
            var currentScreen by remember { mutableStateOf("dashboard") }
            
            Scaffold(
                containerColor = Color(0xFF0F0F0F),
                bottomBar = {
                    NavigationBar(
                        containerColor = Color(0xFF1E1E1E),
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == "dashboard",
                            onClick = { currentScreen = "dashboard" },
                            icon = { Icon(Icons.Default.Home, "Dashboard") },
                            label = { Text("Home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00C853),
                                selectedTextColor = Color(0xFF00C853),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFF00C853).copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == "transactions",
                            onClick = { currentScreen = "transactions" },
                            icon = { Icon(Icons.Default.List, "Transactions") },
                            label = { Text("Cards") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00C853),
                                selectedTextColor = Color(0xFF00C853),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFF00C853).copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == "insights",
                            onClick = { currentScreen = "insights" },
                            icon = { Icon(Icons.Default.Insights, "Insights") },
                            label = { Text("Insights") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00C853),
                                selectedTextColor = Color(0xFF00C853),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFF00C853).copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == "goal",
                            onClick = { currentScreen = "goal" },
                            icon = { Icon(Icons.Default.Star, "Goal") },
                            label = { Text("Goal") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00C853),
                                selectedTextColor = Color(0xFF00C853),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFF00C853).copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (currentScreen) {
                        "dashboard" -> DashboardScreen()
                        "transactions" -> TransactionScreen()
                        "insights" -> InsightsScreen(onBack = { currentScreen = "dashboard" })
                        "goal" -> GoalScreen(onBack = { currentScreen = "dashboard" })
                    }
                }
            }
        }
    }
}