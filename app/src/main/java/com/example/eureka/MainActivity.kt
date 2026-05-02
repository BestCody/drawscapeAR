package com.example.eureka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.eureka.ui.navigation.ARDrawNavHost
import com.example.eureka.ui.theme.ARDrawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ARDrawTheme {
                ARDrawNavHost()
            }
        }
    }
}
