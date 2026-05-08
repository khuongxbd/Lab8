package com.example.notegk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notegk.ui.screens.LoginScreen
import com.example.notegk.ui.screens.MainAdminScreen
import com.example.notegk.ui.theme.NoteGKTheme
import com.example.notegk.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteGKTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                val startDestination = if (authViewModel.uiState.value.isLoggedIn) "admin" else "login"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("login") {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate("admin") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("admin") {
                        MainAdminScreen(
                            authViewModel = authViewModel,
                            onLogoutSuccess = {
                                navController.navigate("login") {
                                    popUpTo("admin") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}