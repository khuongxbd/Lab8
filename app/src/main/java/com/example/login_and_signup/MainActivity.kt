package com.example.login_and_signup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.login_and_signup.ui.theme.Login_and_SignupTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Login_and_SignupTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Mynavigation()
                }
            }
        }
    }
}

@Composable
fun Mynavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Signin.route
    ){
        composable(Screen.Signin.route){
            SignIn(navController = navController)
        }
        composable( Screen.Home.route){
            HomeScreen(navController = navController)
        }
        composable(Screen.Signup.route){
            SignUp(navController = navController)
        }
    }
}
