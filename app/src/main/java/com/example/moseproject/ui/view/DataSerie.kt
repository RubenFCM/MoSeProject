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

@Composable
fun DataSerie(navController: NavController, id: String?) {
    val model: SerieIDViewModel = viewModel()
    val videos: VideoViewModel = viewModel()
    val recommendations: RecomendationsViewModel = viewModel()
    LaunchedEffect(id) {
        if (id != null) {
            model.getSerieById(id)
            videos.getVideoSerieById(id)
            recommendations.getRecomendationsSerie(id)
        }
    }
    val serie by model.serie.collectAsState()
    val loading by model.isLoading.collectAsState()
    val video by videos.video.collectAsState()
    val recommended by recommendations.series.collectAsState()
    val item = serie
    if (item == null) {
        DetailUnavailable(navController, loading)
        return
    }
    LazyColumn(Modifier.fillMaxSize().navigationBarsPadding(), contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            DetailHero(item.name, item.poster_path, item.backdrop_path, "Serie",
                listOfNotNull(item.first_air_date.orEmpty().take(4), "${item.number_of_seasons} temporadas", item.origin_country.orEmpty().firstOrNull()).joinToString("  ·  "),
                item.vote_average, navController)
        }
        item { GenreChips(item.genres.orEmpty().map { it.name }) }
        item { OverviewSection(item.overview.orEmpty()) }
        video?.results?.firstOrNull()?.let { trailer ->
            item {
                SectionHeading("Tráiler en español")
                Box(Modifier.padding(horizontal = 12.dp)) { VideoPlayerExo(trailer.key, LocalLifecycleOwner.current) }
            }
        }
        item { Cast(navController, item.id.toString(), ScreenType.SERIES) }
        item {
            SectionHeading("Sobre la serie")
            InfoPanel(listOf(
                "Primer episodio" to formatCinemaDate(item.first_air_date),
                "Última emisión" to formatCinemaDate(item.last_air_date),
                "Temporadas" to item.number_of_seasons.toString(),
                "Episodios" to item.number_of_episodes.toString(),
                "Estado" to when (item.status) {
                    "Returning Series" -> "En emisión"
                    "Ended" -> "Finalizada"
                    "Canceled" -> "Cancelada"
                    "In Production" -> "En producción"
                    else -> item.status.orEmpty()
                }
            ))
        }
        if (!item.production_companies.isNullOrEmpty()) item { Companies(item.production_companies) }
        if (!item.networks.isNullOrEmpty()) item { Networks(item.networks) }
        if (recommended.isNotEmpty()) {
            item { SectionHeading("Tu siguiente serie") }
            item { PosterRow(recommended.flatMap { it.results }.map { it.toCinemaItem() }, navController) }
        }
    }
}
