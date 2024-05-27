package com.example.moseproject.ui.view


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.ui.view.components.BackImage
import com.example.moseproject.ui.view.components.Companies
import com.example.moseproject.ui.view.components.ImageOnImage
import com.example.moseproject.ui.view.components.Networks
import com.example.moseproject.ui.view.components.RecomendationsSerie
import com.example.moseproject.ui.view.components.TitleOnImage
import com.example.moseproject.ui.view.components.VideoPlayerExo
import com.example.moseproject.ui.viewmodel.RecomendationsViewModel
import com.example.moseproject.ui.viewmodel.SerieIDViewModel
import com.example.moseproject.ui.viewmodel.VideoViewModel

@Composable
fun DataSerie(navController: NavController, id : String?){

    val  screenType = ScreenType.SERIES

    val serieIdViewModel: SerieIDViewModel = viewModel()
    val videoViewModel : VideoViewModel = viewModel()
    var recomendationsSerieViewModel : RecomendationsViewModel = viewModel()

    if (id != null){
        serieIdViewModel.getSerieById(id)
        videoViewModel.getVideoSerieById(id)
        recomendationsSerieViewModel.getRecomendationsSerie(id)
    }

    val serie = serieIdViewModel.serie.collectAsState()

    val companies = serie.value?.production_companies?.map { companie ->
        companie
    }

    val networtk = serie.value?.networks?.map { net ->
        net
    }

    val video = videoViewModel.video.collectAsState()
    var key = ""
    if (video.value != null){
        val videoSpanish = video.value?.results?.map { key ->
            key.key
        }
        key = videoSpanish?.get(0).toString()
    }

    val recomendations by recomendationsSerieViewModel.series.collectAsState(initial= emptyList())

    LazyColumn() {
        item {
            Box (
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
            ){
                BackImage(image = serie.value?.backdrop_path.toString())
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .absoluteOffset(x = 4.dp, y = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                TitleOnImage(title = serie.value?.name.toString())
                ImageOnImage(image = serie.value?.poster_path.toString())
            }
            Column( modifier = Modifier
                .height(140.dp)
                .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ){
                Text(text = "Estrenada: " + serie.value?.first_air_date.toString() +" (ES)" , color = Color.White)
                Text(text = "Origen: " + serie.value?.production_countries?.get(0)?.name.toString(), color = Color.White)
                Text(text = "Puntuación: " + serie.value?.vote_average?.toString(), color = Color.White)
                if (serie.value?.status == "Returning Series"){
                    Text(text = "Estado: Volverá a emitirse", color = Color.White)
                }else{
                    Text(text = "Estado: Finalizado", color = Color.White)
                }
            }
        }
        item {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .border(2.dp, Color.Gray, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        val totalGenres = serie.value?.genres?.size ?: 0
                        serie.value?.genres?.forEachIndexed { index, genre ->
                            Text(
                                text = genre.name + if (index < totalGenres -1) " | " else "",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        item {
            Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 2.dp)
            Column {
                Text(text = serie.value?.name.toString() , modifier = Modifier.padding(bottom = 6.dp), color = Color.White)
                Text(text = serie.value?.overview.toString(), color = Color.LightGray)
            }
            Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 2.dp)
        }
        if (key != ""){
            item {
                Column {
                    Text(text = "Tráiler", color = Color.White)
                    VideoPlayerExo(videoUrl = key , lifecycleOwner = LocalLifecycleOwner.current)
                }
            }
        }
        item{
            Cast(navController,serie.value?.id.toString(),screenType)
        }
        if (companies != null){
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .padding()
                        .border(
                            width = 1.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Companies(companies = companies )
                }
            }
        }
        if (networtk != null){
            item{
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .padding()
                        .border(
                            width = 1.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Networks(net = networtk)
                }
            }
        }
        item {
            RecomendationsSerie(navController = navController, data = recomendations)
        }
    }
}


