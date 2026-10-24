package com.example.moseproject.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.ui.view.components.*
import com.example.moseproject.ui.viewmodel.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DataFilm(navController: NavController, id: String?) {
    val model: FilmIdViewModel = viewModel()
    val videos: VideoViewModel = viewModel()
    val recommendations: RecomendationsViewModel = viewModel()
    LaunchedEffect(id) {
        if (id != null) {
            model.getFilmById(id)
            videos.getVideoFilmById(id)
            recommendations.getRecomendationsFilm(id)
        }
    }
    val film by model.film.collectAsState()
    val loading by model.isLoading.collectAsState()
    val video by videos.video.collectAsState()
    val recommended by recommendations.films.collectAsState()
    val item = film
    if (item == null) {
        DetailUnavailable(navController, loading)
        return
    }
    LazyColumn(Modifier.fillMaxSize().navigationBarsPadding(), contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            DetailHero(item.title, item.poster_path, item.backdrop_path, "Película",
                listOfNotNull(item.release_date.orEmpty().take(4), item.runtime.takeIf { it > 0 }?.let { "$it min" }, item.origin_country.orEmpty().firstOrNull()).joinToString("  ·  "),
                item.vote_average, navController)
        }
        item { GenreChips(item.genres.orEmpty().map { it.name }) }
        item { OverviewSection(item.overview.orEmpty()) }
        video?.results?.firstOrNull()?.let { trailer ->
            item {
                SectionHeading("Tráiler en español")
                Box(Modifier.padding(horizontal = 12.dp)) {
                    VideoPlayerExo(trailer.key, LocalLifecycleOwner.current)
                }
            }
        }
        item { Cast(navController, item.id.toString(), ScreenType.FILM) }
        item {
            SectionHeading("Sobre la película")
            InfoPanel(listOf(
                "Estreno" to formatCinemaDate(item.release_date),
                "Título original" to item.original_title.orEmpty(),
                "Duración" to if (item.runtime > 0) "${item.runtime} minutos" else "No disponible",
                "Presupuesto" to formatBudget(item.budget),
                "Recaudación" to formatBudget(item.revenue)
            ))
        }
        if (!item.production_companies.isNullOrEmpty()) item { Companies(item.production_companies) }
        if (recommended.isNotEmpty()) {
            item { SectionHeading("También te puede gustar") }
            item { PosterRow(recommended.flatMap { it.results }.map { it.toCinemaItem() }, navController) }
        }
    }
}

private fun formatBudget(value: Long): String =
    if (value > 0) NumberFormat.getIntegerInstance(Locale("es", "ES")).format(value) + " $" else "No disponible"
