package com.ibi.moneytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ibi.moneytracker.uiLayer.screen.NavigationScreen
import com.ibi.moneytracker.ui.theme.MoneyTrackerTheme

class MainActivity : ComponentActivity() {
    private val repository by lazy {
        (application as MoneyTrackerApplication).repository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyTrackerTheme {
                NavigationScreen()
            }
        }
    }
}
