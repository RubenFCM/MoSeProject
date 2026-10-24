package com.example.moseproject.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.view.components.*
import com.example.moseproject.ui.viewmodel.*

@Composable
fun Series(navController: NavController) {
    val trendingModel: TrendingSerieViewModel = viewModel()
    val todayModel: AiringTodayViewModel = viewModel()
    val topModel: TopRatedSeriesViewmodel = viewModel()
    LaunchedEffect(Unit) {
        trendingModel.getSeriesTrending()
        todayModel.getSeriesToday()
        topModel.getSeriesTopRated()
    }
    val trending by trendingModel.listSeries.collectAsState()
    val today by todayModel.series.collectAsState()
    val top by topModel.series.collectAsState()
    val loading by topModel.isLoading.collectAsState()
    val trendingItems = trending.flatMap { it.results }.map { it.toCinemaItem() }
    val todayItems = today.flatMap { it.results }.map { it.toCinemaItem() }
    val topItems = top.flatMap { it.results }.map { it.toCinemaItem() }
    CinemaScaffold(navController, AppScreen.SerieScreen.route, "Series", AppScreen.SearhScreenSerie.route) { padding ->
        if (loading && topItems.isEmpty()) Progress(Modifier.padding(padding))
        else if (trendingItems.isEmpty() && todayItems.isEmpty() && topItems.isEmpty())
            EmptyState("No hemos podido cargar las series", "Comprueba tu conexión y vuelve a abrir esta pantalla.", Modifier.padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 28.dp + LocalCinemaNavigationClearance.current)) {
            item { ScreenIntro("Un capítulo más", "Imposible\nquedarse en uno.", "Descubre las series de las que todo el mundo habla.") }
            trendingItems.firstOrNull()?.let { featured -> item { FeaturedCard(featured, navController, "SERIE DESTACADA") } }
            item { SectionHeading("Nuevos capítulos") { navController.navigate(AppScreen.SeriesToday.route) } }
            item { PosterRow(todayItems, navController) }
            item { SectionHeading("En tendencia") { navController.navigate(AppScreen.SeriesTrending.route) } }
            item { PosterRow(trendingItems, navController) }
            item { SectionHeading("Las mejor valoradas") { navController.navigate(AppScreen.SeriesTop.route) } }
            item { PosterRow(topItems, navController) }
        }
    }
}
