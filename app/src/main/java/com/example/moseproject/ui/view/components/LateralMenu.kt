package com.example.moseproject.ui.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.R
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.navigation.AppScreen

@Composable
fun LateralMenu(navController: NavController, screenType: ScreenType){
    ModalDrawerSheet(drawerContainerColor = colorResource(id = R.color.lateral_menu)) {
        Column(modifier = Modifier.fillMaxHeight().width(240.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically){
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.mipmap.ic_launcher_round)
                        .crossfade(true)
                        .scale(Scale.FIT)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .height(64.dp)
                        .padding(6.dp)
                )
                Text(
                    text = "Cine verse",
                    modifier = Modifier
                        .padding(start = 6.dp),
                    fontSize = 28.sp,
                    color = Color.White
                )
            }
            ItemLateralMenu(navController = navController, text = "Inicio", icon = R.drawable.ic_lateral_home, screenType)
            ItemLateralMenu(navController = navController, text = "Películas", icon = R.drawable.ic_lateral_film, screenType)
            ItemLateralMenu(navController = navController, text = "Series", icon = R.drawable.ic_lateral_serie, screenType )
            ItemLateralMenu(navController = navController, text = "Gente Popular", icon = R.drawable.ic_people, screenType )
        }
    }
}

@Composable
fun ItemLateralMenu(navController: NavController,text:String, icon: Int, screenType: ScreenType){
    val isSelected = when (text) {
        "Inicio" -> screenType == ScreenType.HOME
        "Películas" -> screenType == ScreenType.FILM
        "Series" -> screenType == ScreenType.SERIES
        "Gente Popular" -> screenType == ScreenType.PEOPLE
        else -> false
    }
    Row(modifier = Modifier
            .clickable {
                when(text){
                    "Inicio" -> { navController.navigate(AppScreen.HomeScreen.route)  }
                    "Películas" -> { navController.navigate(AppScreen.FilmScreen.route) }
                    "Series" -> { navController.navigate(AppScreen.SerieScreen.route) }
                    "Gente Popular" -> { navController.navigate(AppScreen.PeopleScreen.route) }
                }
            }
        .width(200.dp)
        .padding(horizontal = 20.dp , vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,

        ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = if (isSelected) Color.White else Color.Gray
        )
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.Gray
        )
    }
}