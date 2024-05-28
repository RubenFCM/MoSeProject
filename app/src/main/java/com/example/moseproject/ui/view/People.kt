package com.example.moseproject.ui.view

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.view.components.LateralMenu
import com.example.moseproject.ui.view.components.Progress
import com.example.moseproject.ui.view.components.Topbar
import com.example.moseproject.ui.viewmodel.PeopleViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun People(navController: NavController){
    var currentScreen = ScreenType.PEOPLE

    val peopleViewModel : PeopleViewModel = viewModel()

    peopleViewModel.getPeople()

    val people by peopleViewModel.listPeople.collectAsState(initial = emptyList())
    val isLoading by peopleViewModel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerContent = {
        LateralMenu(navController = navController, screenType = currentScreen)
    },drawerState = drawerState) {
        Scaffold(
            containerColor = Color.Black,
            topBar = { Topbar(title = "Gente Popular", navController, currentScreen,drawerState,scope) },
        )
        {
            if (isLoading) {
                Progress()
            }else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), // 3 columnas
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp)
                ) {
                    items(people.size) { index ->
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(route = AppScreen.PersonID.route + "/" + people[index].id)
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
                                            .data(Constants.BASE_URL_IMAGES_W220 + people[index].profile_path)
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
                                    text = people[index].name ,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    text = when (people[index].known_for_department) {
                                        "Acting" -> "Interpretación"
                                        "Directing" -> "Dirección"
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
        }
    }
}