package com.example.android_practice.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_practice.catalog.domain.PokemonRepository
import com.example.android_practice.catalog.presentation.CatalogScreen
import com.example.android_practice.catalog.presentation.CatalogViewModel
import com.example.android_practice.catalog.presentation.DetailsScreen
import com.example.android_practice.catalog.presentation.DetailsViewModel

private object Routes {
    const val CATALOG = "catalog"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    const val DETAILS = "details/{pokemonId}"
    fun details(id: Int) = "details/$id"
}

@Composable
fun AppNavigation(repository: PokemonRepository) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedRoute = if (currentRoute == Routes.DETAILS) Routes.CATALOG else currentRoute

    Scaffold(bottomBar = {
        NavigationBar {
            listOf(
                Triple(Routes.CATALOG, "Catalog", "◉"),
                Triple(Routes.FAVORITES, "Favorites", "★"),
                Triple(Routes.PROFILE, "Profile", "●"),
            ).forEach { (route, label, symbol) ->
                NavigationBarItem(
                    selected = selectedRoute == route,
                    onClick = {
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Text(symbol) },
                    label = { Text(label) },
                )
            }
        }
    }) { innerPadding ->
        NavHost(navController, startDestination = Routes.CATALOG, modifier = Modifier.padding(innerPadding)) {
            composable(Routes.CATALOG) {
                val vm: CatalogViewModel = viewModel(factory = CatalogViewModel.factory(repository))
                val pokemon by vm.pokemon.collectAsStateWithLifecycle()
                CatalogScreen(pokemon, onPokemonClick = { navController.navigate(Routes.details(it)) })
            }
            composable(Routes.FAVORITES) {
                PlaceholderScreen("Favorites", "No favorites yet")
            }
            composable(Routes.PROFILE) {
                PlaceholderScreen("Profile", "Profile is coming soon")
            }
            composable(Routes.DETAILS, arguments = listOf(navArgument("pokemonId") { type = NavType.IntType })) { entry ->
                val id = requireNotNull(entry.arguments).getInt("pokemonId")
                val vm: DetailsViewModel = viewModel(factory = DetailsViewModel.factory(repository, id))
                val pokemon by vm.pokemon.collectAsStateWithLifecycle()
                DetailsScreen(pokemon, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}
