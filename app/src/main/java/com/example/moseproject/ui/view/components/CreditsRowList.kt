package com.example.moseproject.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.theme.*

@Composable
fun CreditsRowList(navController: NavController, ids: List<String>, images: List<String>, names: List<String>, pjs: List<String>) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(minOf(ids.size, images.size, names.size, pjs.size)) { index ->
            Column(Modifier.width(100.dp).clip(RoundedCornerShape(14.dp)).clickable { navController.navigate(AppScreen.PersonID.route + "/" + ids[index]) }) {
                Box(Modifier.fillMaxWidth().height(125.dp).clip(RoundedCornerShape(14.dp)).background(CinemaSurface), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = CinemaMuted)
                    AsyncImage(images[index], names[index], Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                NameAndPj(names[index], pjs[index])
            }
        }
    }
}

@Composable
fun NameAndPj(names: String, subName: String) {
    Column(Modifier.padding(top = 10.dp)) {
        Text(names, color = CinemaText, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(subName, Modifier.padding(top = 3.dp), color = CinemaMuted, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
