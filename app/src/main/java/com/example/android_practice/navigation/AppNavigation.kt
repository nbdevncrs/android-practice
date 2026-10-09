package com.example.android_practice.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_practice.AppContainer
import com.example.android_practice.R
import com.example.android_practice.core.diagnostics.NetworkDiagnosticsButton
import com.example.android_practice.catalog.presentation.list.CatalogScreen
import com.example.android_practice.catalog.presentation.list.CatalogViewModel
import com.example.android_practice.catalog.presentation.details.DetailsScreen
import com.example.android_practice.catalog.presentation.details.DetailsViewModel

private object Routes {
    const val CATALOG_GRAPH = "catalog_graph"
    const val CATALOG = "catalog"
    const val DETAILS = "details/{${DetailsViewModel.POKEMON_ID_KEY}}"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    fun details(id: Int) = "details/$id"
}

private enum class BottomTab(val route: String, @param:StringRes val title: Int, @param:DrawableRes val icon: Int) {
    CATALOG(Routes.CATALOG_GRAPH, R.string.catalog_title, R.drawable.ic_catalog),
    FAVORITES(Routes.FAVORITES, R.string.favorites_title, R.drawable.ic_favorite),
    PROFILE(Routes.PROFILE, R.string.profile_title, R.drawable.ic_profile),
}

@Composable
fun AppNavigation(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val catalogFactory = remember(container) { CatalogViewModel.factory(container.getPokemonPage) }
    val detailsFactory = remember(container) { DetailsViewModel.factory(container.getPokemonDetails) }

    Scaffold(floatingActionButton = { NetworkDiagnosticsButton() }, bottomBar = {
        NavigationBar {
            BottomTab.entries.forEach { tab ->
                val selected = destination?.hierarchy?.any { it.route == tab.route } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                    label = { Text(stringResource(tab.title)) },
                )
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = Routes.CATALOG_GRAPH, modifier = Modifier.padding(padding)) {
            navigation(startDestination = Routes.CATALOG, route = Routes.CATALOG_GRAPH) {
                composable(Routes.CATALOG) { entry ->
                    val vm: CatalogViewModel = viewModel(viewModelStoreOwner = entry, factory = catalogFactory)
                    val uiState by vm.uiState.collectAsStateWithLifecycle()
                    CatalogScreen(
                        uiState = uiState,
                        onRefresh = vm::refresh,
                        onRetry = vm::retry,
                        onPokemonClick = { id ->
                            navController.navigate(Routes.details(id)) { launchSingleTop = true }
                        },
                    )
                }
                composable(
                    route = Routes.DETAILS,
                    arguments = listOf(navArgument(DetailsViewModel.POKEMON_ID_KEY) { type = NavType.IntType }),
                ) { entry ->
                    val vm: DetailsViewModel = viewModel(viewModelStoreOwner = entry, factory = detailsFactory)
                    val uiState by vm.uiState.collectAsStateWithLifecycle()
                    DetailsScreen(uiState, onBack = { navController.popBackStack() },
                        onRefresh = vm::refresh, onRetry = vm::retry)
                }
            }
            composable(Routes.FAVORITES) {
                PlaceholderScreen(BottomTab.FAVORITES, R.string.favorites_placeholder)
            }
            composable(Routes.PROFILE) {
                PlaceholderScreen(BottomTab.PROFILE, R.string.profile_placeholder)
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(tab: BottomTab, @StringRes message: Int) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(tab.icon), contentDescription = null, modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(tab.title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(message), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
