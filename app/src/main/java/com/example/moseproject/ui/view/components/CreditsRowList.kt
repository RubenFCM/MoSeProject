package com.example.moseproject.ui.view.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.navigation.AppScreen

@Composable
fun CreditsRowList(navController: NavController,ids: List<String>,images :List<String>, names :List<String>, pjs: List<String>){

    LazyRow(modifier = Modifier.fillMaxSize()) {
        items(images.size){ res ->
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable {
                        navController.navigate(route = AppScreen.PersonID.route+ "/" + ids[res])
                    }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black
                    ),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(images[res])
                            .crossfade(true)
                            .scale(Scale.FIT)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .height(120.dp)
                            .width(110.dp)
                            .scale(1.2f)
                    )
                }
                NameAndPj(names = names[res], subName = pjs[res] )
            }
        }
    }
}

@Composable
fun NameAndPj( names :String, subName: String){
    Column(
        modifier = Modifier.height(120.dp),
//        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = names,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(110.dp),
            color = Color.White
        )
        Text(
            text = subName,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(110.dp),
            color = Color.Gray
        )
    }
}