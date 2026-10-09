package com.example.android_practice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.android_practice.navigation.AppNavigation
import com.example.android_practice.ui.theme.AndroidpracticeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PracticeApplication).container
        setContent {
            AndroidpracticeTheme {
                AppNavigation(container)
            }
        }
    }
}

