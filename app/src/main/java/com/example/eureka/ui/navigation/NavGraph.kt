package com.example.eureka.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eureka.ui.screens.ar.ARDrawScreen
import com.example.eureka.ui.screens.discover.DiscoverScreen
import com.example.eureka.ui.screens.drawing.DrawingDetailScreen
import com.example.eureka.ui.screens.home.HomeScreen
import com.example.eureka.ui.screens.profile.ProfileScreen
import com.example.eureka.ui.screens.settings.SettingsScreen

sealed class Route(val path: String) {
    data object Home         : Route("home")
    data object ARDraw       : Route("ar_draw")
    data object Discover     : Route("discover")
    data object Profile      : Route("profile")
    data object Settings     : Route("settings")
    data class DrawingDetail(val drawingId: String = "{drawingId}")
        : Route("drawing/{drawingId}") {
        fun buildRoute(id: String) = "drawing/$id"
    }
}

@Composable
fun ARDrawNavHost(
    navController    : NavHostController = rememberNavController(),
    startDestination : String = Route.Home.path
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        composable(Route.Home.path) {
            HomeScreen(
                onStartDrawing = { navController.navigate(Route.ARDraw.path) },
                onOpenDiscover = { navController.navigate(Route.Discover.path) },
                onOpenProfile  = { navController.navigate(Route.Profile.path) },
            )
        }

        composable(Route.ARDraw.path) {
            ARDrawScreen(
                onExit = { navController.popBackStack() }
            )
        }

        composable(Route.Discover.path) {
            DiscoverScreen(
                onDrawingClick = { id -> navController.navigate(Route.DrawingDetail().buildRoute(id)) },
                onBack         = { navController.popBackStack() }
            )
        }

        composable(
            route     = Route.DrawingDetail().path,
            arguments = listOf(navArgument("drawingId") { type = NavType.StringType })
        ) { backStack ->
            val drawingId = backStack.arguments?.getString("drawingId") ?: return@composable
            DrawingDetailScreen(
                drawingId  = drawingId,
                onViewInAR = { navController.navigate(Route.ARDraw.path) },
                onBack     = { navController.popBackStack() }
            )
        }

        composable(Route.Profile.path) {
            ProfileScreen(
                onOpenSettings = { navController.navigate(Route.Settings.path) },
                onBack         = { navController.popBackStack() }
            )
        }

        composable(Route.Settings.path) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
