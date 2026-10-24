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
fun Films(navController: NavController) {
    val popularModel: PopularFilmViewModel = viewModel()
    val upcomingModel: UpcomingFilmViewModel = viewModel()
    val topModel: TopRatedFilmViewModel = viewModel()
    LaunchedEffect(Unit) {
        popularModel.getImagesFilmsPopular()
        upcomingModel.getFilmsUpcoming()
        topModel.getFilmsTopRated()
    }
    val popular by popularModel.films.collectAsState()
    val upcoming by upcomingModel.films.collectAsState()
    val top by topModel.films.collectAsState()
    val loading by topModel.isLoading.collectAsState()
    val popularItems = popular.flatMap { it.results }.map { it.toCinemaItem() }
    val upcomingItems = upcoming.flatMap { it.results }.map { it.toCinemaItem() }
    val topItems = top.flatMap { it.results }.map { it.toCinemaItem() }
    CinemaScaffold(navController, AppScreen.FilmScreen.route, "Películas", AppScreen.SearhScreenFilm.route) { padding ->
        if (loading && topItems.isEmpty()) Progress(Modifier.padding(padding))
        else if (popularItems.isEmpty() && upcomingItems.isEmpty() && topItems.isEmpty())
            EmptyState("No hemos podido cargar las películas", "Comprueba tu conexión y vuelve a abrir esta pantalla.", Modifier.padding(padding))
        else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 28.dp + LocalCinemaNavigationClearance.current)) {
            item { ScreenIntro("En pantalla grande", "Historias que\ndejan huella.", "Estrenos, grandes clásicos y tu próxima película favorita.") }
            popularItems.firstOrNull()?.let { featured -> item { FeaturedCard(featured, navController, "PELÍCULA DESTACADA") } }
            item { SectionHeading("Próximamente") { navController.navigate(AppScreen.UpcomingFilm.route) } }
            item { PosterRow(upcomingItems, navController) }
            item { SectionHeading("Las mejor valoradas") { navController.navigate(AppScreen.TopFilm.route) } }
            item { PosterRow(topItems, navController) }
            item { SectionHeading("Populares ahora") }
            item { PosterRow(popularItems, navController) }
        }
    }
}
