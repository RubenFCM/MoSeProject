package com.example.moseproject.ui.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.view.components.CarouselCard
import com.example.moseproject.ui.view.components.LateralMenu
import com.example.moseproject.ui.view.components.TitleRow
import com.example.moseproject.ui.view.components.Topbar
import com.example.moseproject.ui.viewmodel.AiringTodayViewModel
import com.example.moseproject.ui.viewmodel.TopRatedSeriesViewmodel
import com.example.moseproject.ui.viewmodel.TrendingSerieViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Series(navController:NavController){

    var currentScreen: ScreenType = ScreenType.SERIES

    val trendingSerieViewModel:TrendingSerieViewModel = viewModel()
    val airingTodayViewModel: AiringTodayViewModel = viewModel()
    val topRatedSeriesViewmodel :TopRatedSeriesViewmodel = viewModel()

    trendingSerieViewModel.getSeriesTrending()
    airingTodayViewModel.getSeriesToday()
    topRatedSeriesViewmodel.getSeriesTopRated()

    val seriesTrending by trendingSerieViewModel.listSeries.collectAsState(initial = emptyList())

    val imagesSeriesPopular = seriesTrending.map { image ->
        if (!image.results[0].poster_path.isNullOrBlank()){
            Constants.BASE_URL_IMAGES_W500+image.results[0].poster_path
        }else{
            Constants.IMAGE_NOT_FOUND
        }

    }

    val seriesToday by airingTodayViewModel.series.collectAsState(initial = emptyList())

    val seriesTopRated by topRatedSeriesViewmodel.series.collectAsState(initial = emptyList())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController, screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Series", navController, currentScreen,drawerState,scope) },
        )
        {
            LazyColumn() {
                item {
                    CarouselCard(imagesSeriesPopular)
                }
                item {
                    TitleRow(title = "Nuevos capítulos",navController =navController)
                }
                item {
                    RowListSeries(series = seriesToday, navController = navController)
                }
                item {
                    TitleRow(title = "Trending",navController =navController)
                }
                item {
                    RowListSeries(series = seriesTrending, navController = navController )
                }
                item {
                    TitleRow(title = "Series mejor valoradas", navController = navController)
                }
                item {
                    RowListSeries(series = seriesTopRated, navController = navController )
                }
            }
        }
    }
}