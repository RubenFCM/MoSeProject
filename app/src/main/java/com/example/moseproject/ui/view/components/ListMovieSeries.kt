package com.example.moseproject.ui.view.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.ui.view.components.AllVerticalGrid
import com.example.moseproject.ui.view.components.FilmVerticalGrid
import com.example.moseproject.ui.view.components.LateralMenu
import com.example.moseproject.ui.view.components.Progress
import com.example.moseproject.ui.view.components.SerieVerticalGrid
import com.example.moseproject.ui.view.components.Topbar
import com.example.moseproject.ui.viewmodel.AiringTodayViewModel
import com.example.moseproject.ui.viewmodel.TopRatedFilmViewModel
import com.example.moseproject.ui.viewmodel.TopRatedSeriesViewmodel
import com.example.moseproject.ui.viewmodel.TrendingAllDayViewModel
import com.example.moseproject.ui.viewmodel.TrendingAllViewModel
import com.example.moseproject.ui.viewmodel.TrendingSerieViewModel
import com.example.moseproject.ui.viewmodel.UpcomingFilmViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListAllDayPlus(navController: NavController){
    var currentScreen = ScreenType.HOME

    val trendingAllDayViewModel:TrendingAllDayViewModel = viewModel()

    trendingAllDayViewModel.getAllTrendingDayPlus()

    val movieSeriesDay by trendingAllDayViewModel.allPlus.collectAsState(initial = emptyList())

    val isLoading by trendingAllDayViewModel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController, screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Populares hoy", navController, currentScreen,drawerState,scope) },
        )
        {
            if (isLoading){
                Progress()
            }else{
                AllVerticalGrid(movieSeriesDay = movieSeriesDay, navController = navController)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListAllWeekPlus(navController: NavController){
    var currentScreen = ScreenType.HOME

    val trendingAllWeekViewModel:TrendingAllViewModel = viewModel()

    trendingAllWeekViewModel.getAllTrendingWeekPlus()

    val movieSeriesWeek by trendingAllWeekViewModel.allPlus.collectAsState(initial = emptyList())

    val isLoading by trendingAllWeekViewModel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController, screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Populares esta semana", navController, currentScreen,drawerState,scope) },
        )
        {
            if (isLoading){
                Progress()
            }else{
                AllVerticalGrid(movieSeriesDay = movieSeriesWeek, navController = navController)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListMovieUpcomingPlus(navController: NavController){
    var currentScreen = ScreenType.FILM

    val upcomingFilmViewModel:UpcomingFilmViewModel = viewModel()

    upcomingFilmViewModel.getFilmsUpcomingPlus()

    val movies by upcomingFilmViewModel.filmsPlus.collectAsState(initial = emptyList())
    val isLoading by upcomingFilmViewModel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController, screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Próximamente", navController, currentScreen,drawerState,scope) },
        )
        {
            if (isLoading){
                Progress()
            }else{
                FilmVerticalGrid(movies = movies, navController = navController)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListMovieTopPlus(navController: NavController){
    var currentScreen = ScreenType.FILM

    val topRatedFilmViewModel:TopRatedFilmViewModel = viewModel()

    topRatedFilmViewModel.getFilmsTopRatedPlus()

    val movies by topRatedFilmViewModel.filmsPlus.collectAsState(initial = emptyList())

    val isLoading by topRatedFilmViewModel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController, screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Mejor valoradas", navController, currentScreen,drawerState,scope) },
        )
        {
            if (isLoading){
                Progress()
            }else{
                FilmVerticalGrid(movies = movies, navController = navController)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListSerieToday(navController: NavController){
    var currentScreen = ScreenType.SERIES

    val airingTodayViewModel:AiringTodayViewModel = viewModel()

    airingTodayViewModel.getSeriesTodayPlus()

    val series by airingTodayViewModel.seriesPlus.collectAsState(initial = emptyList())
    val isLoading by airingTodayViewModel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController, screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Nuevos capítulos", navController, currentScreen,drawerState,scope) },
        )
        {
            if (isLoading){
                Progress()
            }else{
                SerieVerticalGrid(series = series, navController = navController)
            }
        }
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListSerieTrending(navController: NavController){
    var currentScreen = ScreenType.SERIES

    val trendingSerieViewModel:TrendingSerieViewModel = viewModel()

    trendingSerieViewModel.getSeriesTrendingPlus()

    val series by trendingSerieViewModel.seriesPlus.collectAsState(initial = emptyList())
    val isLoading by trendingSerieViewModel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController, screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Trending", navController, currentScreen,drawerState,scope) },
        )
        {
            if (isLoading){
                Progress()
            }else{
                SerieVerticalGrid(series = series, navController = navController)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListSerieTop(navController: NavController){
    var currentScreen = ScreenType.SERIES

    val topRatedSeriesViewmodel:TopRatedSeriesViewmodel = viewModel()

    topRatedSeriesViewmodel.getSeriesTopRatedPlus()

    val series by topRatedSeriesViewmodel.seriesPlus.collectAsState(initial = emptyList())
    val isLoading by topRatedSeriesViewmodel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController, screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Mejor valoradas", navController, currentScreen,drawerState,scope) },
        )
        {
            if (isLoading){
                Progress()
            }else{
                SerieVerticalGrid(series = series, navController = navController)
            }
        }
    }
}
