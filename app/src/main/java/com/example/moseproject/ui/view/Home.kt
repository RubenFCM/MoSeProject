package com.example.moseproject.ui.view

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.R
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.view.components.CarouselCard
import com.example.moseproject.ui.view.components.LateralMenu
import com.example.moseproject.ui.view.components.TitleRow
import com.example.moseproject.ui.view.components.Topbar
import com.example.moseproject.ui.viewmodel.TrendingAllDayViewModel
import com.example.moseproject.ui.viewmodel.TrendingAllViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Home(navController: NavController){

    var currentScreen: ScreenType = ScreenType.HOME // Tipo de pantalla en la que me encuentro

    val trendingAllViewModel : TrendingAllViewModel = viewModel()
    val trendingAllDayViewModel : TrendingAllDayViewModel = viewModel()

    trendingAllViewModel.getAllTrendingWeek()
    trendingAllDayViewModel.getAllTrendingDay()

    val allWeek by trendingAllViewModel.all.collectAsState(initial = emptyList())
    var dataWeek = allWeek.map { res ->
        res.results[0]
    }
    var imagesWeek = allWeek.map { res ->
        Constants.BASE_URL_IMAGES_W500+res.results[0].poster_path
    }
    val allDay by trendingAllDayViewModel.all.collectAsState(initial = emptyList())
    val dataDay = allDay.map { res ->
        res.results[0]
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController, screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Cine Verse", navController,currentScreen,drawerState,scope) }
        ){
            LazyColumn() {
                item {
                    CarouselCard(imagesWeek)
                }
                item {
                    TitleRow(title = "Populares hoy",navController)
                }
                item {
                    RowListAll(data = dataDay, navController)
                }
                item {
                    TitleRow(title = "Populares esta semana",navController)
                }
                item {
                    RowListAll(data = dataWeek, navController)
                }
            }
        }
    }
}