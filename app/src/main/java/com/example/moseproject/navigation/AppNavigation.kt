package com.example.moseproject.navigation

import androidx.compose.runtime.Composable
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

@Composable
fun AppNavigation(){
    val navController = rememberNavController() // Gestiona el estado de la navegación entre pantallas
    NavHost(navController = navController, startDestination = AppScreen.HomeScreen.route ){

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
            DataFilm(navController,it.arguments?.getString("id"))
        }

        composable(
                route= AppScreen.SerieID.route+ "/{id}",
                arguments = listOf(navArgument(name = "id"
                ){
                    type = NavType.StringType
                })
            ){
            DataSerie(navController,it.arguments?.getString("id"))
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