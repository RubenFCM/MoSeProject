package com.example.moseproject.ui.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.data.model.Result
import com.example.moseproject.data.model.ResultAll
import com.example.moseproject.data.model.ResultSerie
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.navigation.AppScreen

@Composable
fun AllVerticalGrid(movieSeriesDay:List<ResultAll> , navController:NavController){
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 columnas
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp)
    ) {
        items(movieSeriesDay.size) { index ->
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .clickable {
                        if (movieSeriesDay[index].media_type == "movie"){
                            navController.navigate(route = AppScreen.FilmID.route+ "/" + movieSeriesDay[index].id)
                        }
                        if (movieSeriesDay[index].media_type == "tv"){
                            navController.navigate(route = AppScreen.SerieID.route+ "/" + movieSeriesDay[index].id)
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Constants.BASE_URL_IMAGES_W220 + movieSeriesDay[index].poster_path)
                                .crossfade(true)
                                .scale(Scale.FIT)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .height(150.dp)
                                .width(120.dp)
                                .scale(1.2f, 1.2f)
                        )
                    }
                    Text(
                        text = movieSeriesDay[index].title ?: movieSeriesDay[index].name ,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = when (movieSeriesDay[index].media_type) {
                            "movie" -> "Película"
                            "tv" -> "Seríe"
                            else -> "Desconocido"
                        } ,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilmVerticalGrid(movies:List<Result>, navController:NavController){
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 columnas
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp)
    ) {
        items(movies.size) { index ->
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .clickable {
                            navController.navigate(route = AppScreen.FilmID.route+ "/" + movies[index].id)
                        },
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Constants.BASE_URL_IMAGES_W220 + movies[index].poster_path)
                                .crossfade(true)
                                .scale(Scale.FIT)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .height(150.dp)
                                .width(120.dp)
                                .scale(1.2f, 1.2f)
                        )
                    }
                    Text(
                        text = movies[index].title,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SerieVerticalGrid(series:List<ResultSerie>, navController:NavController){
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 columnas
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp)
    ) {
        items(series.size) { index ->
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(route = AppScreen.SerieID.route+ "/" + series[index].id)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Constants.BASE_URL_IMAGES_W220 + series[index].poster_path)
                                .crossfade(true)
                                .scale(Scale.FIT)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .height(150.dp)
                                .width(120.dp)
                                .scale(1.2f, 1.2f)
                        )
                    }
                    Text(
                        text = series[index].name,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}