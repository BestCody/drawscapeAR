package com.example.eureka
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.eureka.theme.ARDrawTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ARDrawTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {

    var isLoggedIn by remember { mutableStateOf(false) }

    // START ON AUTH SCREEN
    var currentScreen by remember { mutableStateOf(AppScreen.AUTH) }

    when (currentScreen) {

        AppScreen.DRAW -> {
            ARDrawScreen(
                onOpenProfile = {
                    currentScreen =
                        if (isLoggedIn) AppScreen.PROFILE
                        else AppScreen.AUTH
                }
            )
        }

        AppScreen.PROFILE -> {
            ProfileScreen(
                username = "@Hansen",
                photoCount = 12,
                onBackToDraw = {
                    currentScreen = AppScreen.DRAW
                }
            )
        }

        AppScreen.AUTH -> {
            AuthScreen(
                onLoginSuccess = {
                    isLoggedIn = true
                    currentScreen = AppScreen.PROFILE
                },
                onBackToDraw = {
                    currentScreen = AppScreen.DRAW
                }
            )
        }
    }
}

enum class AppScreen {
    DRAW,
    PROFILE,
    AUTH
}