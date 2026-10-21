package com.example.moseproject.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.view.components.*
import com.example.moseproject.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController) {
    val weekModel: TrendingAllViewModel = viewModel()
    val dayModel: TrendingAllDayViewModel = viewModel()
    LaunchedEffect(Unit) {
        weekModel.getAllTrendingWeek()
        dayModel.getAllTrendingDay()
    }
    val week by weekModel.all.collectAsState()
    val day by dayModel.all.collectAsState()
    val loading by weekModel.isLoading.collectAsState()
    val weekItems = week.flatMap { it.results }.filter { it.media_type == "movie" || it.media_type == "tv" }.map { it.toCinemaItem() }
    val dayItems = day.flatMap { it.results }.filter { it.media_type == "movie" || it.media_type == "tv" }.map { it.toCinemaItem() }

    CinemaScaffold(navController, AppScreen.HomeScreen.route, "Descubre", AppScreen.SearhScreenFilm.route) { padding ->
        if (loading && weekItems.isEmpty()) {
            Progress(Modifier.padding(padding))
        } else if (weekItems.isEmpty() && dayItems.isEmpty()) {
            EmptyState("No hemos podido cargar el catálogo", "Comprueba tu conexión y vuelve a abrir esta pantalla.", Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 28.dp)) {
                item { ScreenIntro("El cine empieza aquí", "Tu próxima\ngran historia.", "Películas, series y las personas que las hacen posibles.") }
                item {
                    Row(Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = true, onClick = {}, label = { Text("Para ti") })
                        FilterChip(selected = false, onClick = { navController.navigate(AppScreen.FilmScreen.route) }, label = { Text("Películas") })
                        FilterChip(selected = false, onClick = { navController.navigate(AppScreen.SerieScreen.route) }, label = { Text("Series") })
                    }
                }
                weekItems.firstOrNull()?.let { featured -> item { FeaturedCard(featured, navController, "TENDENCIA DE LA SEMANA") } }
                item { SectionHeading("Lo más visto hoy") { navController.navigate(AppScreen.AllDay.route) } }
                item { PosterRow(dayItems, navController) }
                item { SectionHeading("Esta semana") { navController.navigate(AppScreen.AllWeek.route) } }
                item { PosterRow(weekItems, navController) }
            }
        }
    }
}
