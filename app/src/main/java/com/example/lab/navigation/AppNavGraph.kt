package com.example.lab.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab.ui.screens.CampingDetailScreen
import com.example.lab.ui.screens.CampingsListScreen
import com.example.lab.ui.screens.FavoritesScreen
import com.example.lab.data.Campings
import com.example.lab.viewmodel.FavoritesViewModel

@Composable
fun AppNavGraph(campings: List<Campings>) {
    val navController = rememberNavController()
    val favViewModel: FavoritesViewModel = viewModel()

    var campingsWithDistance by remember { mutableStateOf(campings) }

    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            CampingsListScreen(
                campings = campingsWithDistance,
                onCampingsUpdated = { updatedList -> campingsWithDistance = updatedList },
                favViewModel = favViewModel,
                onCampingClick = { id -> navController.navigate(Routes.detailRoute(id)) },
                onFavoritesClick = { navController.navigate(Routes.FAVORITES) }
            )
        }

        composable(
            route = Routes.DETAIL_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val campingId = backStackEntry.arguments?.getString(Routes.ARG_ID)
            val selected  = campingsWithDistance.firstOrNull { it.signatura == campingId }
            CampingDetailScreen(
                camping = selected,
                favViewModel = favViewModel,
                onBack = { navController.popBackStack() },
                onFavoritesClick = { navController.navigate(Routes.FAVORITES) }
            )
        }

        composable(Routes.FAVORITES) {
            FavoritesScreen(
                favViewModel = favViewModel,
                onCampingClick = { id -> navController.navigate(Routes.detailRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}