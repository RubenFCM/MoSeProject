package com.example.moseproject.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.data.model.FilmResult
import com.example.moseproject.R
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.navigation.AppScreen

@Composable
fun RowListFilm(films :List<FilmResult>,navController: NavController){
    // Formatear el valor de la puntuación
    val ratings = films.map { rating ->
        val voteAverage = rating.results[0].vote_average
        val formattedRating = if(voteAverage == 0.0){
            "n/a"
        }else if (voteAverage < 10) {
            String.format("%.1f", voteAverage)
        } else {
            "10"
        }
        formattedRating
    }
    LazyRow(modifier = Modifier.fillMaxSize()) {
        items(films.size){ film ->
            Card(
                modifier = Modifier
                    .width(240.dp)
                    .height(400.dp)
                    .padding(horizontal = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Card(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ){
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Constants.BASE_URL_IMAGES_W220 + films[film].results[0].poster_path)
                                .crossfade(true)
                                .scale(Scale.FIT)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .height(280.dp)
                                .width(240.dp)
                                .scale(1.2f, 1f)
                                .clickable { navController.navigate(route = AppScreen.FilmID.route + "/" + films[film].results[0].id) }
                        )
                    }

                    Row() {
                        Text(
                            text = films[film].results[0].title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold ,
                            modifier = Modifier
                                .width(180.dp)
                                .height(90.dp)
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            textAlign = TextAlign.Left
                        )
                        Text(
                            text = ratings[film],
                            color = colorResource(id = R.color.rating),
                            modifier = Modifier
                                .padding(top = 34.dp),
                            textAlign = TextAlign.End,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                    Text(
                        text = films[film].results[0].release_date,
                        modifier = Modifier
                            .padding(start = 4.dp),
                        color = Color.White,
                        textAlign = TextAlign.End,
                        fontSize = 18.sp,
                    )
                }
            }
        }
    }
}


