package com.example.moseproject.ui.view

import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
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
import com.example.moseproject.ui.view.components.CarouselCard
import com.example.moseproject.ui.view.components.LateralMenu
import com.example.moseproject.ui.view.components.RowListAll
import com.example.moseproject.ui.view.components.TitleRow
import com.example.moseproject.ui.view.components.Topbar
import com.example.moseproject.ui.viewmodel.TrendingAllDayViewModel
import com.example.moseproject.ui.viewmodel.TrendingAllViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Home(navController: NavController){

    val currentScreen: ScreenType = ScreenType.HOME // Tipo de pantalla en la que me encuentro

    val trendingAllViewModel : TrendingAllViewModel = viewModel()
    val trendingAllDayViewModel : TrendingAllDayViewModel = viewModel()

    trendingAllViewModel.getAllTrendingWeek()
    trendingAllDayViewModel.getAllTrendingDay()

    val allWeek by trendingAllViewModel.all.collectAsState(initial = emptyList())
    val dataWeek = allWeek.map { res ->
        res.results[0]
    }
    val imagesWeek = allWeek.map { res ->
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