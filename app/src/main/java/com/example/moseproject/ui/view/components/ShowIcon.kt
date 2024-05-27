package com.example.moseproject.ui.view.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.data.model.Network
import com.example.moseproject.data.model.ProductionCompany
import com.example.moseproject.data.utils.Constants

@Composable
fun Companies(companies:List<ProductionCompany>){
    // Estado para controlar si la columna está expandida o no, que por defecto será contraída
    var columnExpanded by remember { mutableStateOf(false) }
    if (!companies.isNullOrEmpty()){
        Column( modifier = Modifier
            .padding( 6.dp)
        ) {
            Text(
                text = "Producida por:",
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 16.sp,
                color = Color.LightGray
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxSize()
                    .width(254.dp)
                    .padding(top = 4.dp)
                    .clip(shape = RoundedCornerShape(8.dp))
                    .clickable { columnExpanded = !columnExpanded },
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                /*
                Valor que por defecto contenga los dos primeros datos de una lista
                si encuentra el estado ,anteriormente definido,  comprimido
                y si no contiene todo los datos
                */
                val companiesToShow = if (columnExpanded) {
                    companies
                } else {
                    companies.take(2)
                }

                companiesToShow.forEach{ companie ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {
                        Text(
                            text = companie.name,
                            modifier = Modifier
                                .padding(start = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        ShowIcon(icon = companie.logo_path)
                    }
                }
                if (companies.size >2){
                    // Icono para indicar si la columna está expandida o comprimida
                    val icon = if (columnExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }

}
@Composable
fun Networks(net:List<Network>){
    // Estado para controlar si la columna está expandida o no, que por defecto será contraída
    var columnExpanded by remember { mutableStateOf(false) }
    if (!net.isNullOrEmpty()){
        Column( modifier = Modifier
            .padding(6.dp)
        ) {
            Text(
                text = "Emitida en:",
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 16.sp,
                color = Color.LightGray
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxSize()
                    .width(254.dp)
                    .padding(top = 4.dp)
                    .clip(shape = RoundedCornerShape(8.dp))
                    .clickable { columnExpanded = !columnExpanded },
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                /*
                Valor que por defecto contenga los dos primeros datos de una lista
                si encuentra el estado ,anteriormente definido,  comprimido
                y si no contiene todo los datos
                */
                val netwotkToShow = if (columnExpanded) {
                    net
                } else {
                    net.take(2)
                }
                netwotkToShow.forEach{ net ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = net.name,
                            modifier = Modifier
                                .padding(start = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        ShowIcon(icon = net.logo_path)
                    }
                }
                // Icono para indicar si la columna está expandida o comprimida
                if (net.size > 2){
                    val icon = if (columnExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ShowIcon(icon :String?) {
    val imageUrl = if (icon.isNullOrEmpty()) {
        Constants.IMAGE_NOT_FOUND
    } else {
        Constants.BASE_URL_IMAGES_W500 + icon
    }
    Card(modifier = Modifier
        .height(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ){
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data( imageUrl )
                .crossfade(true)
                .scale(Scale.FILL)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .height(28.dp)
                .padding(4.dp)
        )
    }
}