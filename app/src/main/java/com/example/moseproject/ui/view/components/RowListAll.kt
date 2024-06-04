package com.example.moseproject.ui.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.R
import com.example.moseproject.data.model.ResultAll
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.navigation.AppScreen

@Composable
fun RowListAll(data: List<ResultAll>,navController:NavController){

    LazyRow(modifier = Modifier.fillMaxSize()) {
        items(data.size){ res ->
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(Constants.BASE_URL_IMAGES_W220+data[res].poster_path)
                            .crossfade(true)
                            .scale(Scale.FIT)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .height(300.dp)
                            .width(190.dp)
                            .scale(1.2f)
                            .clickable {
                                if (data[res].media_type == "movie"){
                                    navController.navigate( route = AppScreen.FilmID.route+ "/" + data[res].id)
                                }
                                if (data[res].media_type == "tv"){
                                    navController.navigate( route = AppScreen.SerieID.route+ "/" + data[res].id)
                                }
                            }
                    )
                }
            }
        }
    }
}