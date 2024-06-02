package com.example.moseproject.ui.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.R
import com.example.moseproject.data.model.FilmResult
import com.example.moseproject.data.model.SerieResult
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.view.TextInfoTitle

@Composable
fun RecomendationsFilm(navController: NavController, data: List<FilmResult>) {
    Spacer(modifier = Modifier.height(16.dp))
    TextInfoTitle(text = "Recomendaciones")
    LazyRow(modifier = Modifier.fillMaxSize()) {
        items(data.size) { res ->
            val film = data[res]
            val posterPath = film.results[0].poster_path
            if (!posterPath.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.lateral_menu)
                    ),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Constants.BASE_URL_IMAGES_W220 + posterPath)
                                .crossfade(true)
                                .scale(Scale.FIT)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .height(210.dp)
                                .width(140.dp)
                                .scale(1f)
                                .clickable {
                                    navController.navigate(
                                        route = AppScreen.FilmID.route + "/" + film.results[0].id
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecomendationsSerie(navController: NavController, data: List<SerieResult>) {
    Spacer(modifier = Modifier.height(16.dp))
    TextInfoTitle(text = "Recomendaciones")
    LazyRow(modifier = Modifier.fillMaxSize()) {
        items(data.size) { res ->
            val serie = data[res]
            val posterPath = serie.results[0].poster_path
            if (!posterPath.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.lateral_menu)
                    ),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Constants.BASE_URL_IMAGES_W220 + posterPath)
                                .crossfade(true)
                                .scale(Scale.FIT)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .height(210.dp)
                                .width(140.dp)
                                .scale(1f)
                                .clickable {
                                    navController.navigate(
                                        route = AppScreen.SerieID.route + "/" + serie.results[0].id
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}
