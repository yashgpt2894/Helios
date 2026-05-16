package com.helios.core.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.helios.feature.battery.BatteryScreen
import com.helios.feature.dashboard.DashboardScreen
import com.helios.feature.insights.InsightsScreen
import com.helios.feature.landing.LandingScreen
import com.helios.feature.production.ProductionScreen
import com.helios.feature.settings.SettingsScreen
import com.helios.feature.shared.SharedScreen

/**
 * Helios NAV — central NavHost with 6 destinations + duplicate LANDING for deep-link fallback.
 * Handles 2 explicit deep-link patterns: https://helios.app/share/{payload} & helios://share/{payload}
 */
object HeliosRoutes {
    const val DASHBOARD = "dashboard"
    const val PRODUCTION = "production"
    const val INSIGHTS = "insights"
    const val BATTERY = "battery"
    const val SETTINGS = "settings"
    const val LANDING = "landing"
    const val SHARED = "shared/{payload}"
}

@Composable
fun HeliosNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = HeliosRoutes.LANDING
    ) {
        // Dashboard (tab home)
        composable(HeliosRoutes.DASHBOARD) {
            DashboardScreen()
        }

        // Production (tab)
        composable(HeliosRoutes.PRODUCTION) {
            ProductionScreen()
        }

        // Insights (tab)
        composable(HeliosRoutes.INSIGHTS) {
            InsightsScreen()
        }

        // Battery (tab)
        composable(HeliosRoutes.BATTERY) {
            BatteryScreen()
        }

        // Settings (tab)
        composable(HeliosRoutes.SETTINGS) {
            SettingsScreen()
        }

        // Deep-link shared viewer
        composable(
            route = "shared/{payload}",
            arguments = listOf(navArgument("payload") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://helios.app/share/{payload}" },
                navDeepLink { uriPattern = "helios://share/{payload}" }
            )
        ) { backStackEntry ->
            val payload = backStackEntry.arguments?.getString("payload") ?: ""
            SharedScreen(encodedPayload = payload)
        }

        // Landing – also absorbs deep-link fallback
        composable(
            route = HeliosRoutes.LANDING,
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://helios.app/" },
                navDeepLink { uriPattern = "helios://" }
            )
        ) {
            LandingScreen()
        }
    }
}
