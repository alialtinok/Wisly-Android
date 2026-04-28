package com.alialtinok.lexiup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alialtinok.lexiup.ui.navigation.MainScreen
import com.alialtinok.lexiup.ui.theme.LexiUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LexiUpTheme {
                MainScreen()
            }
        }
    }
}
