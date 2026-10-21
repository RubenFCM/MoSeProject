package com.example.moseproject.ui.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.moseproject.data.model.KnownForResult
import com.example.moseproject.data.model.PersonResult
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.theme.*
import com.example.moseproject.ui.view.components.*
import com.example.moseproject.ui.viewmodel.*
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DataPerson(navController: NavController, id: String?) {
    val model: PersonIDViewModel = viewModel()
    val creditsModel: KnowForViewModel = viewModel()
    LaunchedEffect(id) {
        if (id != null) {
            model.getPerson(id)
            creditsModel.getCombinedCredits(id)
        }
    }
    val person by model.dataPerson.collectAsState()
    val loading by model.isLoading.collectAsState()
    val credits by creditsModel.combinedCredits.collectAsState()
    CinemaScaffold(navController, AppScreen.PeopleScreen.route, "Perfil", back = true) { padding ->
        val data = person
        if (data == null) {
            if (loading) Progress(Modifier.padding(padding))
            else EmptyState("Perfil no disponible", "Comprueba tu conexión y vuelve a intentarlo.", Modifier.padding(padding))
        } else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 32.dp)) {
            item {
                Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    AsyncImage(imageUrl(data.profile_path), data.name, Modifier.width(112.dp).height(168.dp)
                        .clip(RoundedCornerShape(18.dp)).background(CinemaSurface), contentScale = ContentScale.Crop)
                    Column(Modifier.weight(1f).padding(top = 10.dp)) {
                        Text("PERSONAS QUE INSPIRAN", style = MaterialTheme.typography.labelSmall, color = CinemaAccent)
                        Text(data.name, Modifier.padding(top = 10.dp), style = MaterialTheme.typography.headlineMedium)
                        Text(departmentName(data.known_for_department), Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyMedium, color = CinemaMuted)
                    }
                }
            }
            if (!data.biography.isNullOrBlank()) item { Biography(data.biography) }
            item { InfoPerson(data) }
            if (!credits?.cast.isNullOrEmpty()) item {
                KnowFor(emptyList(), credits!!, navController)
            }
        }
    }
}

private fun departmentName(department: String?) = when (department) {
    "Acting" -> "Interpretación"
    "Directing" -> "Dirección"
    "Writing" -> "Guion"
    "Production" -> "Producción"
    else -> department ?: "Cine y televisión"
}

fun formatDateAndCalculateAge(birthday: String?): String = runCatching {
    val birth = LocalDate.parse(birthday)
    val age = Period.between(birth, LocalDate.now()).years
    val date = birth.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES")))
    "$date ($age años)"
}.getOrDefault("No disponible")

@Composable
fun Biography(biography: String) {
    var expanded by remember(biography) { mutableStateOf(false) }
    SectionHeading("Su historia")
    Column(Modifier.padding(horizontal = 20.dp).animateContentSize()) {
        Text(biography, style = MaterialTheme.typography.bodyLarge, color = CinemaMuted,
            maxLines = if (expanded) Int.MAX_VALUE else 5, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        if (biography.length > 250) TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Leer menos" else "Leer biografía completa")
        }
    }
}

@Composable
fun InfoPerson(person: PersonResult?) {
    if (person == null) return
    SectionHeading("Información personal")
    val details = mutableListOf(
        "Profesión" to departmentName(person.known_for_department),
        "Nacimiento" to if (person.deathday.isNullOrBlank()) formatDateAndCalculateAge(person.birthday) else formatCinemaDate(person.birthday),
        "Lugar de nacimiento" to person.place_of_birth?.takeIf { it.isNotBlank() }.orEmpty().ifBlank { "No disponible" },
        "Género" to when (person.gender) { 1 -> "Femenino"; 2 -> "Masculino"; 3 -> "No binario"; else -> "No indicado" }
    )
    if (!person.deathday.isNullOrBlank()) details.add("Fallecimiento" to formatCinemaDate(person.deathday))
    InfoPanel(details)
}

@Composable
fun TextInfoTitle(text: String) = SectionHeading(text)

@Composable
fun KnowFor(images: List<String>, credits: KnownForResult, navController: NavController) {
    SectionHeading("Su trayectoria en pantalla")
    PosterRow(credits.cast.filter { (it.media_type == "movie" || it.media_type == "tv") && !it.poster_path.isNullOrBlank() }
        .distinctBy { it.media_type + it.id }.map { it.toCinemaItem() }, navController)
}
