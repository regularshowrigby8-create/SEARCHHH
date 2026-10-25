package com.searchhh.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.searchhh.app.di.AppContainer
import com.searchhh.app.ui.detail.DetailRoute
import com.searchhh.app.ui.detail.DetailViewModel
import com.searchhh.app.ui.home.HomeRoute
import com.searchhh.app.ui.home.HomeViewModel
import com.searchhh.app.ui.results.ResultsRoute
import com.searchhh.app.ui.results.ResultsViewModel
import com.searchhh.app.ui.saved.SavedRoute
import com.searchhh.app.ui.saved.SavedViewModel
import com.searchhh.app.ui.settings.SettingsRoute
import com.searchhh.app.ui.settings.SettingsViewModel

/**
 * Single NavHost + bottom bar for the three top-level destinations.
 * Results and Detail are pushed on top and hide the bar.
 */
@Composable
fun SearchhhNavGraph(
    container: AppContainer,
    deepLinkQuery: String? = null,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(deepLinkQuery) {
        if (!deepLinkQuery.isNullOrBlank()) {
            navController.navigate(Routes.results(deepLinkQuery))
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentRoute in Routes.topLevel) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = { navController.navigateTopLevel(Routes.HOME) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SAVED,
                        onClick = { navController.navigateTopLevel(Routes.SAVED) },
                        icon = { Icon(Icons.Filled.Bookmark, contentDescription = "Saved") },
                        label = { Text("Saved") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SETTINGS,
                        onClick = { navController.navigateTopLevel(Routes.SETTINGS) },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                val viewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.factory(
                        container.searchRepository,
                        container.savedRepository,
                        container.recentQueriesRepository,
                        container.dispatchers,
                    ),
                )
                HomeRoute(
                    viewModel = viewModel,
                    onSearch = { query, category -> navController.navigate(Routes.results(query, category)) },
                    onOpenEntry = { id -> navController.navigate(Routes.detail(id)) },
                )
            }

            composable(
                route = Routes.RESULTS,
                arguments = listOf(
                    navArgument(Routes.ARG_QUERY) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(Routes.ARG_CATEGORY) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { entry ->
                val query = entry.arguments?.getString(Routes.ARG_QUERY).orEmpty()
                val category = entry.arguments?.getString(Routes.ARG_CATEGORY)?.takeIf { it.isNotBlank() }
                val viewModel: ResultsViewModel = viewModel(
                    factory = ResultsViewModel.factory(
                        searchRepository = container.searchRepository,
                        savedRepository = container.savedRepository,
                        dispatchers = container.dispatchers,
                        query = query,
                        category = category,
                    ),
                )
                ResultsRoute(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenEntry = { id -> navController.navigate(Routes.detail(id)) },
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument(Routes.ARG_ID) { type = NavType.StringType },
                ),
            ) { entry ->
                val id = entry.arguments?.getString(Routes.ARG_ID).orEmpty()
                val viewModel: DetailViewModel = viewModel(
                    factory = DetailViewModel.factory(
                        searchRepository = container.searchRepository,
                        savedRepository = container.savedRepository,
                        dispatchers = container.dispatchers,
                        entryId = id,
                    ),
                )
                DetailRoute(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable(Routes.SAVED) {
                val viewModel: SavedViewModel = viewModel(
                    factory = SavedViewModel.factory(container.savedRepository),
                )
                SavedRoute(
                    viewModel = viewModel,
                    onOpenEntry = { id -> navController.navigate(Routes.detail(id)) },
                )
            }

            composable(Routes.SETTINGS) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.factory(
                        settingsRepository = container.settingsRepository,
                        searchRepository = container.searchRepository,
                        savedRepository = container.savedRepository,
                        recentQueriesRepository = container.recentQueriesRepository,
                        dispatchers = container.dispatchers,
                        updateBaseUrl = container::updateBaseUrl,
                    ),
                )
                SettingsRoute(viewModel = viewModel)
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
