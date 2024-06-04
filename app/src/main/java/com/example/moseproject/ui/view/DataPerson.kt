package com.example.moseproject.ui.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.data.model.PersonResult
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.ui.viewmodel.PersonIDViewModel
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.moseproject.R
import com.example.moseproject.data.model.KnownForResult
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.viewmodel.KnowForViewModel

@Composable
fun DataPerson(navController: NavController, id: String?){

    val personIDViewModel : PersonIDViewModel = viewModel()
    val knowForViewModel : KnowForViewModel = viewModel()

    if (id != null) {
        personIDViewModel.getPerson(id)
        knowForViewModel.getCombinedCredits(id)
    }

    val person = personIDViewModel.dataPerson.collectAsState()
    val credits = knowForViewModel.combinedCredits.collectAsState()

    val photo = if (!person.value?.profile_path.isNullOrBlank()){
        Constants.BASE_URL_IMAGES_W220 + person.value?.profile_path
    } else {
        Constants.IMAGE_NOT_FOUND_CREDITS
    }

    val images = credits.value?.cast?.map {
        if (it.poster_path.isNullOrBlank()){
            Constants.IMAGE_NOT_FOUND_CREDITS
        }else{
            Constants.BASE_URL_IMAGES_W220+it.poster_path
        }
    }.orEmpty()

    LazyColumn() {
        item {
            Box (
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .absoluteOffset(x = 4.dp, y = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(route = AppScreen.PeopleScreen.route) },
                        modifier = Modifier
                            .absoluteOffset(y = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_people),
                            contentDescription = "Volver a pantalla de gente popular",
                            tint = Color.White
                        )
                    }
                }

            }
        }
        item {
            Column (
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Card(
                    modifier = Modifier
                        .height(180.dp)
                        .width(120.dp)
                ){
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photo)
                            .crossfade(true)
                            .scale(Scale.FIT)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = person.value?.name.toString(),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            InfoPerson(person = person.value)
        }
        item {
            if (!person.value?.biography.isNullOrBlank()){
                Biography(biography = person.value?.biography.toString())
            }
        }
        item {
            if (!credits.value?.cast.isNullOrEmpty()){
                credits.value?.let { KnowFor(images = images, credits = it, navController = navController) }
            }
        }
    }
}

fun formatDateAndCalculateAge(birthday: String?): String {
    if (birthday.isNullOrEmpty()) return "Fecha desconocida"

    // Formato esperado de la fecha de cumpleaños (ISO 8601, ej. "1982-05-03")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val birthDate = LocalDate.parse(birthday, formatter)
    val currentDate = LocalDate.now()
    val age = Period.between(birthDate, currentDate).years

    // Formato de salida (ej. "3 de mayo de 1982 (42 años)")
    val outputFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    return "${birthDate.format(outputFormatter)} ($age años)"
}

@Composable
fun Biography(biography : String){
    // Estado para controlar la expansión
    var expanded by remember { mutableStateOf(false) }
    Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 2.dp)
    Column (
        modifier = Modifier
            .padding(4.dp)
            .clickable { expanded = !expanded }
            .animateContentSize(),
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextInfoTitle(text = "Biografía")
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
        if (expanded) {
            Text(text = biography, color = Color.LightGray)
        } else {
            Text(
                text = biography,
                color = Color.LightGray,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis // Añadir puntos suspensivos al final...
            )
        }
    }
    Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 2.dp)
}
@Composable
fun InfoPerson(person:PersonResult?){
    Column (
        modifier = Modifier
            .height(320.dp)
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 4.dp)
            .background(colorResource(id = R.color.lateral_menu)),
        verticalArrangement = Arrangement.SpaceAround
    ){
        TextInfoTitle("Información personal")
        Text(text = "Conocido por", color = Color.White,fontSize = 16.sp,fontWeight = FontWeight.Bold)
        Text(text = when (person?.known_for_department) {
            "Acting" -> "Interpretación"
            "Directing" -> "Dirección"
            else -> "Desconocido"
        }, color = Color.LightGray)
        Text(text = "Sexo", color = Color.White,fontSize = 16.sp,fontWeight = FontWeight.Bold)
        Text(text = when (person?.gender) {
            1 -> "Femenino"
            2 -> "Masculino"
            else -> "Desconocido"
        }, color = Color.LightGray)
        Text(text = "Fecha de nacimiento", color = Color.White,fontSize = 16.sp,fontWeight = FontWeight.Bold)
        if (!person?.birthday.isNullOrBlank()){
            val birthdayText = formatDateAndCalculateAge(person?.birthday)
            Text(text = birthdayText, color = Color.LightGray)
        }else{
            Text(text = "No actualizado", color = Color.LightGray)
        }

        if (!person?.deathday.isNullOrBlank()){
            val deathDayText = formatDateAndCalculateAge(person?.deathday)
            Text(text = "Fecha de fallecimiento", color = Color.White,fontSize = 16.sp,fontWeight = FontWeight.Bold)
            Text(text = deathDayText,color = Color.LightGray)
        }
        Text(text = "Lugar de nacimiento", color = Color.White,fontSize = 16.sp,fontWeight = FontWeight.Bold)
        if (!person?.place_of_birth.isNullOrBlank()){
            Text(text = person?.place_of_birth.toString() , color = Color.LightGray)
        }else{
            Text(text = "No actualizado", color = Color.LightGray)
        }
    }
}

@Composable
fun TextInfoTitle(text : String){
    Text(
        text = text,
        modifier = Modifier.padding(bottom = 6.dp),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}
@Composable
fun KnowFor(images : List<String>, credits: KnownForResult, navController : NavController){
    TextInfoTitle(text = "Conocido por")
    LazyRow(modifier = Modifier.fillMaxSize()) {
        items(images.size){ res ->
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.lateral_menu)
                ),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(images[res])
                            .crossfade(true)
                            .scale(Scale.FIT)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .height(210.dp)
                            .width(140.dp)
                            .scale(1f)
                            .clickable {
                                if (credits.cast.get(res).media_type == "movie") {
                                    navController.navigate(
                                        route = AppScreen.FilmID.route + "/" + credits.cast.get(
                                            res
                                        ).id
                                    )
                                }
                                if (credits.cast.get(res).media_type == "tv") {
                                    navController.navigate(
                                        route = AppScreen.SerieID.route + "/" + credits.cast.get(
                                            res
                                        ).id
                                    )
                                }
                            }
                    )
                }
            }
        }
    }
}