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
import com.example.moseproject.data.model.ProductionCompany
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.ui.view.components.BackImage
import com.example.moseproject.ui.view.components.Companies
import com.example.moseproject.ui.view.components.ImageOnImage
import com.example.moseproject.ui.view.components.Progress
import com.example.moseproject.ui.view.components.RecomendationsFilm
import com.example.moseproject.ui.view.components.TitleOnImage
import com.example.moseproject.ui.view.components.VideoPlayerExo
import com.example.moseproject.ui.viewmodel.FilmIdViewModel
import com.example.moseproject.ui.viewmodel.RecomendationsViewModel
import com.example.moseproject.ui.viewmodel.VideoViewModel


@Composable
fun DataFilm(navController: NavController, id : String?){

    val  screenType = ScreenType.FILM

    var filmIdViewModel: FilmIdViewModel = viewModel()
    var videoViewModel : VideoViewModel = viewModel()
    var recomendationsFilmViewModel : RecomendationsViewModel = viewModel()

    if (id != null) {
        filmIdViewModel.getFilmById(id)
        videoViewModel.getVideoFilmById(id)
        recomendationsFilmViewModel.getRecomendationsFilm(id)
    }

    val film = filmIdViewModel.film.collectAsState()
    var companies : List<ProductionCompany> = emptyList()
    if (film.value?.production_companies != null) {
        companies = film.value?.production_companies?.map { companie ->
            companie
        }!!
    }

    val video = videoViewModel.video.collectAsState()
    var key = ""
    if (video.value != null){
        val videoSpanish = video.value?.results?.map { key ->
            key.key
        }
        key = videoSpanish?.get(0).toString()
    }

    val recomendations by recomendationsFilmViewModel.films.collectAsState(initial = emptyList())
    val isLoading by filmIdViewModel.isLoading.collectAsState()
    val isLoadingVideo by videoViewModel.isLoadingVideo.collectAsState()
    if (isLoading || isLoadingVideo){
        Progress()
    }else{
        LazyColumn() {
            item {
                Box (
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth()
                ){
                    BackImage(image = film.value?.backdrop_path.toString())
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
                    TitleOnImage(title = film.value?.title.toString())
                    ImageOnImage(image = film.value?.poster_path.toString())
                }
                Column( modifier = Modifier
                    .height(140.dp)
                    .padding(bottom = 4.dp),
                    verticalArrangement = Arrangement.SpaceAround
                ){
                    Text(text = "Estreno: " + film.value?.release_date.toString() +" (ES)" , color = Color.White)
                    Text(text = "Presupuesto: " + (film.value?.budget?.takeIf { it != 0 } ?: "n/a"), color = Color.White)
                    Text(text = "Recaudado: " + (film.value?.budget?.takeIf { it != 0 } ?: "n/a"), color = Color.White)
                    Row {
                        Text(text = "Origen: " + film.value?.origin_country?.get(0), color = Color.White)
                        Spacer(modifier = Modifier.padding(start = 24.dp))
                        Text(text = "Puntuación: " + film.value?.vote_average , color = Color.White)
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
                            val totalGenres = film.value?.genres?.size ?: 0
                            film.value?.genres?.forEachIndexed { index, genre ->
                                Text(
                                    text = genre.name + if (index < totalGenres -1) " | " else "",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    Text(text = film.value?.runtime.toString()+" min",modifier = Modifier.padding(top = 6.dp), color = Color.White,fontSize = 14.sp)
                }
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 2.dp)
                Column {
                    Text(text = film.value?.title.toString() , modifier = Modifier.padding(bottom = 6.dp), color = Color.White)
                    Text(text = film.value?.overview.toString(), color = Color.LightGray)
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
                Cast(navController,film.value?.id.toString(),screenType)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .padding()
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    if (!companies.isNullOrEmpty()) {
                        Companies(companies = companies)
                    }
                }
            }
            item {
                if (!recomendations.isNullOrEmpty()){
                    RecomendationsFilm(navController,recomendations)
                }
            }
        }
    }

}





