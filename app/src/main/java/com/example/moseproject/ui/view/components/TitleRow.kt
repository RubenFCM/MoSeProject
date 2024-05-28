package com.example.moseproject.ui.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moseproject.R
import com.example.moseproject.navigation.AppScreen

@Composable
fun TitleRow(title:String, navController : NavController){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .clickable {
                if (title =="Populares hoy"){
                    navController.navigate(route = AppScreen.AllDay.route)
                }
                if (title == "Populares esta semana"){
                    navController.navigate(route = AppScreen.AllWeek.route)
                }
                if (title == "Próximamente"){
                    navController.navigate(route = AppScreen.UpcomingFilm.route)
                }
                if (title == "Películas mejor valoradas"){
                    navController.navigate(route = AppScreen.TopFilm.route)
                }
                if (title == "Nuevos capítulos"){
                    navController.navigate(route = AppScreen.SeriesToday.route)
                }
                if (title == "Trending"){
                    navController.navigate(route = AppScreen.SeriesTrending.route)
                }
                if (title == "Series mejor valoradas"){
                    navController.navigate(route = AppScreen.SeriesTop.route)
                }
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

    ){
        Text(
            text = title,
            color = colorResource(id = R.color.title_row),
            fontSize = 22.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Ver más",
            color = colorResource(id = R.color.title_row),
            fontSize = 16.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}