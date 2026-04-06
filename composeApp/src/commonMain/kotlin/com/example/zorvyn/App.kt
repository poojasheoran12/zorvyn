package com.example.zorvyn

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zorvyn.di.appModule
import com.example.zorvyn.presentation.AuthViewModel
import com.example.zorvyn.presentation.ui.*
import com.example.zorvyn.util.getBiometricAuthenticator
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.KoinAppDeclaration

@Composable
@Preview
fun App(koinAppDeclaration: KoinAppDeclaration? = null) {
    KoinApplication(application = {
        koinAppDeclaration?.invoke(this)
        modules(appModule)
    }) {
        val authViewModel: AuthViewModel = koinViewModel()
        val user by authViewModel.user.collectAsState()
        
        var isLocked by remember { mutableStateOf(true) }
        var authError by remember { mutableStateOf<String?>(null) }
        val authenticator = remember { getBiometricAuthenticator() }

        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF00C853),
                secondary = Color(0xFF00C853),
                background = Color(0xFF0F0F0F),
                surface = Color(0xFF1E1E1E),
                onBackground = Color.White,
                onSurface = Color.White
            )
        ) {
            if (user == null) {
                LoginScreen(onLoginSuccess = { 
                    // Session established
                })
            } else {
                LaunchedEffect(Unit) {
                    if (isLocked) {
                        authenticator.authenticate(
                            title = "Unlock Zorvyn",
                            subtitle = "Use biometrics to access your financial data",
                            onSuccess = { isLocked = false },
                            onError = { authError = it }
                        )
                    }
                }

                if (isLocked) {
                    LockScreen(authError) {
                        authenticator.authenticate(
                            title = "Unlock Zorvyn",
                            subtitle = "Access your data",
                            onSuccess = { isLocked = false },
                            onError = { authError = it }
                        )
                    }
                } else {
                    MainLayout()
                }
            }
        }
    }
}

@Composable
fun LockScreen(error: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Lock, "Locked", modifier = Modifier.size(80.dp), tint = Color(0xFF00C853))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Zorvyn Locked", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        if (error != null) {
            Text(error, color = Color.Red, modifier = Modifier.padding(16.dp))
        }
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
        ) {
            Text("Unlock App")
        }
    }
}

@Composable
fun MainLayout() {
    var currentScreen by remember { mutableStateOf("dashboard") }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    selected = currentScreen == "plan",
                    onClick = { currentScreen = "plan" },
                    icon = { Icon(Icons.Default.DateRange, "Plan") },
                    label = { Text("Plan") },
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
                    selected = currentScreen == "profile",
                    onClick = { currentScreen = "profile" },
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") },
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
        Box(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
            when (currentScreen) {
                "dashboard" -> DashboardScreen()
                "plan" -> PlanningScreen(onBack = { currentScreen = "dashboard" })
                "insights" -> InsightsScreen(onBack = { currentScreen = "dashboard" })
                "profile" -> ProfileScreen(
                    onBack = { currentScreen = "dashboard" },
                    onLogout = { /* App will recompose and show LoginScreen */ }
                )
                else -> DashboardScreen()
            }
        }
    }
}