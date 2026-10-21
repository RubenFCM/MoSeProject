package com.example.moseproject.ui.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.viewmodel.*

@Composable
fun ListAllDayPlus(navController: NavController) {
    val model: TrendingAllDayViewModel = viewModel()
    LaunchedEffect(Unit) { model.getAllTrendingDayPlus() }
    val items by model.allPlus.collectAsState()
    val loading by model.isLoading.collectAsState()
    CinemaScaffold(navController, AppScreen.HomeScreen.route, "Populares hoy", AppScreen.SearhScreenFilm.route, back = true) { padding ->
        if (loading) Progress(Modifier.padding(padding))
        else PosterGrid(items.map { it.toCinemaItem() }.distinctBy { it.kind + it.id }, navController, Modifier.padding(padding))
    }
}

@Composable
fun ListAllWeekPlus(navController: NavController) {
    val model: TrendingAllViewModel = viewModel()
    LaunchedEffect(Unit) { model.getAllTrendingWeekPlus() }
    val items by model.allPlus.collectAsState()
    val loading by model.isLoading.collectAsState()
    CinemaScaffold(navController, AppScreen.HomeScreen.route, "Esta semana", AppScreen.SearhScreenFilm.route, back = true) { padding ->
        if (loading) Progress(Modifier.padding(padding))
        else PosterGrid(items.map { it.toCinemaItem() }.distinctBy { it.kind + it.id }, navController, Modifier.padding(padding))
    }
}

@Composable
fun ListMovieUpcomingPlus(navController: NavController) {
    val model: UpcomingFilmViewModel = viewModel()
    LaunchedEffect(Unit) { model.getFilmsUpcomingPlus() }
    val items by model.filmsPlus.collectAsState()
    val loading by model.isLoading.collectAsState()
    CinemaScaffold(navController, AppScreen.FilmScreen.route, "Próximamente", AppScreen.SearhScreenFilm.route, back = true) { padding ->
        if (loading) Progress(Modifier.padding(padding))
        else PosterGrid(items.map { it.toCinemaItem() }.distinctBy { it.kind + it.id }, navController, Modifier.padding(padding))
    }
}

@Composable
fun ListMovieTopPlus(navController: NavController) {
    val model: TopRatedFilmViewModel = viewModel()
    LaunchedEffect(Unit) { model.getFilmsTopRatedPlus() }
    val items by model.filmsPlus.collectAsState()
    val loading by model.isLoading.collectAsState()
    CinemaScaffold(navController, AppScreen.FilmScreen.route, "Películas mejor valoradas", AppScreen.SearhScreenFilm.route, back = true) { padding ->
        if (loading) Progress(Modifier.padding(padding))
        else PosterGrid(items.map { it.toCinemaItem() }.distinctBy { it.kind + it.id }, navController, Modifier.padding(padding))
    }
}

@Composable
fun ListSerieToday(navController: NavController) {
    val model: AiringTodayViewModel = viewModel()
    LaunchedEffect(Unit) { model.getSeriesTodayPlus() }
    val items by model.seriesPlus.collectAsState()
    val loading by model.isLoading.collectAsState()
    CinemaScaffold(navController, AppScreen.SerieScreen.route, "Nuevos capítulos", AppScreen.SearhScreenSerie.route, back = true) { padding ->
        if (loading) Progress(Modifier.padding(padding))
        else PosterGrid(items.map { it.toCinemaItem() }.distinctBy { it.kind + it.id }, navController, Modifier.padding(padding))
    }
}

@Composable
fun ListSerieTrending(navController: NavController) {
    val model: TrendingSerieViewModel = viewModel()
    LaunchedEffect(Unit) { model.getSeriesTrendingPlus() }
    val items by model.seriesPlus.collectAsState()
    val loading by model.isLoading.collectAsState()
    CinemaScaffold(navController, AppScreen.SerieScreen.route, "Series en tendencia", AppScreen.SearhScreenSerie.route, back = true) { padding ->
        if (loading) Progress(Modifier.padding(padding))
        else PosterGrid(items.map { it.toCinemaItem() }.distinctBy { it.kind + it.id }, navController, Modifier.padding(padding))
    }
}

@Composable
fun ListSerieTop(navController: NavController) {
    val model: TopRatedSeriesViewmodel = viewModel()
    LaunchedEffect(Unit) { model.getSeriesTopRatedPlus() }
    val items by model.seriesPlus.collectAsState()
    val loading by model.isLoading.collectAsState()
    CinemaScaffold(navController, AppScreen.SerieScreen.route, "Series mejor valoradas", AppScreen.SearhScreenSerie.route, back = true) { padding ->
        if (loading) Progress(Modifier.padding(padding))
        else PosterGrid(items.map { it.toCinemaItem() }.distinctBy { it.kind + it.id }, navController, Modifier.padding(padding))
    }
}
