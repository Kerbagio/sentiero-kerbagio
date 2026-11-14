// UI layer (navigation): the map of screens and how to move between them. Screens only
// receive callbacks like onOpenHike(id); this file decides what those callbacks do,
// so no screen needs to know about the NavController.
// Back uses navigateUp(), never popBackStack(): a fast double tap on popBackStack()
// would also remove Upcoming and leave a blank screen.
package com.anthony.sentiero.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.anthony.sentiero.ui.detail.HikeDetailScreen
import com.anthony.sentiero.ui.detail.HikeDetailViewModel
import com.anthony.sentiero.ui.form.HikeFormScreen
import com.anthony.sentiero.ui.form.HikeFormViewModel
import com.anthony.sentiero.ui.upcoming.UpcomingScreen
import com.anthony.sentiero.ui.upcoming.UpcomingViewModel

@Composable
fun SentieroNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Upcoming) {
        composable<Upcoming> {
            UpcomingScreen(
                viewModel = viewModel(factory = UpcomingViewModel.Factory),
                onNewHike = { navController.navigate(HikeForm()) },
                onOpenHike = { id -> navController.navigate(HikeDetail(id)) },
            )
        }
        composable<HikeForm> {
            HikeFormScreen(
                viewModel = viewModel(factory = HikeFormViewModel.Factory),
                onBack = { navController.navigateUp() },
                // Open the new hike and remove the form, so Back goes to Upcoming.
                onSaved = { id ->
                    navController.navigate(HikeDetail(id)) {
                        popUpTo<HikeForm> { inclusive = true }
                    }
                },
            )
        }
        composable<HikeDetail> { entry ->
            val id = entry.toRoute<HikeDetail>().id
            HikeDetailScreen(
                viewModel = viewModel(factory = HikeDetailViewModel.factory(id)),
                onBack = { navController.navigateUp() },
            )
        }
    }
}
