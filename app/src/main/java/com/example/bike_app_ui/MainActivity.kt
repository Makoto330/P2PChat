package com.example.bike_app_ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bike_app_ui.Auth.AuthScreen
import com.example.bike_app_ui.Chat.ChatScreen
import com.example.bike_app_ui.ui.theme.BikeappuiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BikeappuiTheme {
                Surface(modifier = Modifier.fillMaxSize(),color = MaterialTheme.colorScheme.background) {
                    AllScreenNavigation()
                }
            }
        }
    }
}

@Composable
fun AllScreenNavigation(){
    val navController = rememberNavController()

    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth"){
            AuthScreen(
                onLoginSuccess = {user ->
                    Toast.makeText(
                        context,
                        "ようこそ！ ${user.name}さん",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate("chat/${user.name}") {
                        popUpTo("auth") {inclusive = true}
                    }

                }
            )
        }

        composable("chat/{userName}") { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "ゲスト"

            ChatScreen(
                currentUserName = userName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}