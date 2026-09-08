package com.flowvid.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flowvid.app.domain.model.FeedSource
import com.flowvid.app.ui.favorites.FavoritesScreen
import com.flowvid.app.ui.feed.FeedScreen
import com.flowvid.app.ui.folders.FolderFilterScreen
import com.flowvid.app.ui.home.HomeScreen
import com.flowvid.app.ui.search.SearchScreen
import com.flowvid.app.ui.settings.SettingsScreen

@Composable
fun FlowVidNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Destinations.HOME) {
        composable(Destinations.HOME) {
            HomeScreen(
                onOpenSearch = { navController.navigate(Destinations.SEARCH) },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
                onOpenFavorites = { navController.navigate(Destinations.FAVORITES) },
                onOpenFolders = { navController.navigate(Destinations.FOLDERS) },
            )
        }

        composable(Destinations.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onOpenVideo = { video ->
                    navController.navigate(
                        Destinations.feedRoute(Destinations.FEED_SOURCE_LIBRARY, video.stableKey),
                    )
                },
            )
        }

        composable(Destinations.FAVORITES) {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onOpenVideo = { video ->
                    navController.navigate(
                        Destinations.feedRoute(Destinations.FEED_SOURCE_FAVORITES, video.stableKey),
                    )
                },
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenFolders = { navController.navigate(Destinations.FOLDERS) },
            )
        }

        composable(Destinations.FOLDERS) {
            FolderFilterScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Destinations.FEED_ROUTE,
            arguments = listOf(
                navArgument(Destinations.FEED_SOURCE_ARG) { type = NavType.StringType },
                navArgument(Destinations.FEED_START_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val sourceArg = backStackEntry.arguments?.getString(Destinations.FEED_SOURCE_ARG)
            val startId = backStackEntry.arguments?.getString(Destinations.FEED_START_ID_ARG)
                ?.let { android.net.Uri.decode(it) }
            val source = if (sourceArg == Destinations.FEED_SOURCE_FAVORITES) FeedSource.Favorites else FeedSource.Library

            FeedScreen(
                source = source,
                startVideoId = startId,
                showBackButton = true,
                onBack = { navController.popBackStack() },
                onOpenSearch = { navController.navigate(Destinations.SEARCH) },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
                onOpenFavorites = { navController.navigate(Destinations.FAVORITES) },
                onOpenFolders = { navController.navigate(Destinations.FOLDERS) },
            )
        }
    }
}
