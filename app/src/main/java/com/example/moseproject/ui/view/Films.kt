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
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.view.components.CarouselCard
import com.example.moseproject.ui.view.components.LateralMenu
import com.example.moseproject.ui.view.components.TitleRow
import com.example.moseproject.ui.view.components.Topbar
import com.example.moseproject.ui.viewmodel.PopularFilmViewModel
import com.example.moseproject.ui.viewmodel.TopRatedFilmViewModel
import com.example.moseproject.ui.viewmodel.UpcomingFilmViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Films(navController: NavController){

    var currentScreen: ScreenType = ScreenType.FILM // Tipo de pantalla en la que me encuentro

    val popularFilmViewModel: PopularFilmViewModel = viewModel()
    val upcomingFilmViewModel: UpcomingFilmViewModel = viewModel()
    val topRatedFilmViewModel: TopRatedFilmViewModel = viewModel()

    popularFilmViewModel.getImagesFilmsPopular()
    upcomingFilmViewModel.getFilmsUpcoming()
    topRatedFilmViewModel.getFilmsTopRated()

    val imageUrlsPopular by popularFilmViewModel.listImages.collectAsState(initial = emptyList())

    val filmsUpcoming by upcomingFilmViewModel.films.collectAsState(initial = emptyList())

    val filmsTop by topRatedFilmViewModel.films.collectAsState(initial = emptyList())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController,screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Películas", navController,currentScreen,drawerState,scope) }
        )
        {
            LazyColumn() {
                item {
                    CarouselCard(imageUrlsPopular)
                }
                item {
                    TitleRow(title = "Próximamente",navController)
                }
                item {
                    RowListFilm(films = filmsUpcoming, navController = navController)
                }
                item {
                    TitleRow(title = "Películas mejor valoradas",navController)
                }
                item {
                    RowListFilm(films = filmsTop, navController = navController )
                }
            }
        }
    }
}


