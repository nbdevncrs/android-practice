package com.example.android_practice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.android_practice.catalog.data.MockPokemonRepository
import com.example.android_practice.navigation.AppNavigation
import com.example.android_practice.ui.theme.AndroidpracticeTheme

class MainActivity : ComponentActivity() {
    private val repository = MockPokemonRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidpracticeTheme {
                AppNavigation(repository)
            }
        }
    }
}
