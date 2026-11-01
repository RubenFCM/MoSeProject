package com.example.moseproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moseproject.ui.view.DataFilm
import com.example.moseproject.ui.view.DataPerson
import com.example.moseproject.ui.view.DataSerie
import com.example.moseproject.ui.view.Films
import com.example.moseproject.ui.view.Home
import com.example.moseproject.ui.view.components.ListAllDayPlus
import com.example.moseproject.ui.view.components.ListAllWeekPlus
import com.example.moseproject.ui.view.components.ListMovieTopPlus
import com.example.moseproject.ui.view.components.ListMovieUpcomingPlus
import com.example.moseproject.ui.view.components.ListSerieToday
import com.example.moseproject.ui.view.components.ListSerieTop
import com.example.moseproject.ui.view.components.ListSerieTrending
import com.example.moseproject.ui.view.People
import com.example.moseproject.ui.view.SearchFilm
import com.example.moseproject.ui.view.SearchPerson
import com.example.moseproject.ui.view.SearchSerie
import com.example.moseproject.ui.view.Series
import com.example.moseproject.ui.view.auth.AccountRoute
import com.example.moseproject.ui.viewmodel.AuthViewModel
import com.example.moseproject.ui.viewmodel.WatchlistViewModel
import com.example.moseproject.ui.view.watchlist.WatchlistScreen
import com.example.moseproject.data.watchlist.MediaType

@Composable
fun AppNavigation(authViewModel: AuthViewModel, watchlistViewModel: WatchlistViewModel){
    val navController = rememberNavController() // Gestiona el estado de la navegación entre pantallas
    val watchState by watchlistViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(watchState.resumeKey) {
        watchState.resumeKey?.let { key ->
            watchlistViewModel.consumeResume()
            navController.navigate((if (key.type == MediaType.Movie) AppScreen.FilmID.route else AppScreen.SerieID.route) + "/${key.tmdbId}") {
                popUpTo(AppScreen.Account.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    NavHost(navController = navController, startDestination = AppScreen.HomeScreen.route ){
        composable(AppScreen.Watchlist.route) { WatchlistScreen(navController, watchlistViewModel, authViewModel) }
        composable(AppScreen.Account.route) {
            AccountRoute(authViewModel, onBrowse = {
                if (!navController.popBackStack()) {
                    navController.navigate(AppScreen.HomeScreen.route) { launchSingleTop = true }
                }
            }, onWatchlist = { navController.navigate(AppScreen.Watchlist.route) { launchSingleTop = true } })
        }

        composable(route= AppScreen.HomeScreen.route){
            Home(navController)
        }

        composable(route= AppScreen.SerieScreen.route){
            Series(navController)
        }

        composable(route= AppScreen.FilmScreen.route){
            Films(navController)
        }

        composable(route = AppScreen.PeopleScreen.route){
            People(navController)
        }

        composable(route= AppScreen.SearhScreenFilm.route){
            SearchFilm(navController)
        }

        composable(route= AppScreen.SearhScreenSerie.route){
            SearchSerie(navController)
        }

        composable(route= AppScreen.SearchPerson.route){
            SearchPerson(navController)
        }
        composable(route= AppScreen.AllDay.route){
            ListAllDayPlus(navController)
        }
        composable(route= AppScreen.AllWeek.route){
            ListAllWeekPlus(navController)
        }

        composable(route= AppScreen.UpcomingFilm.route){
            ListMovieUpcomingPlus(navController)
        }
        composable(route= AppScreen.TopFilm.route){
            ListMovieTopPlus(navController)
        }
        composable(route= AppScreen.SeriesToday.route){
            ListSerieToday(navController)
        }
        composable(route= AppScreen.SeriesTrending.route){
            ListSerieTrending(navController)
        }

        composable(route= AppScreen.SeriesTop.route){
            ListSerieTop(navController)
        }

        composable(
                route= AppScreen.FilmID.route+ "/{id}",
                arguments = listOf(navArgument(name = "id"
                ){
                    type = NavType.StringType
                })
            ){
            DataFilm(navController,it.arguments?.getString("id"), watchlistViewModel, authViewModel)
        }

        composable(
                route= AppScreen.SerieID.route+ "/{id}",
                arguments = listOf(navArgument(name = "id"
                ){
                    type = NavType.StringType
                })
            ){
            DataSerie(navController,it.arguments?.getString("id"), watchlistViewModel, authViewModel)
        }

        composable(
                route= AppScreen.PersonID.route+ "/{id}",
                arguments = listOf(navArgument(name = "id"
                ){
                    type = NavType.StringType
                })
            ){
            DataPerson(navController,it.arguments?.getString("id"))
        }
    }
}
