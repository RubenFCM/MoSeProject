package com.example.moseproject.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.theme.*
import com.example.moseproject.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaSearch(navController: NavController, kind: String) {
    val model: SearchViewModel = viewModel()
    var query by rememberSaveable { mutableStateOf("") }
    val films by model.films.collectAsState()
    val series by model.series.collectAsState()
    val people by model.people.collectAsState()
    val loading by model.isLoading.collectAsState()
    val error by model.error.collectAsState()
    LaunchedEffect(query) {
        model.clearSearch()
        if (query.isNotBlank()) {
            delay(400)
            when (kind) {
                "movie" -> model.getFilmsSearch(query.trim())
                "tv" -> model.getSeriesSearch(query.trim())
                else -> model.getPersonSearch(query.trim())
            }
        }
    }
    val items = when (kind) {
        "movie" -> films.flatMap { it.results }.map { it.toCinemaItem() }
        "tv" -> series.flatMap { it.results }.map { it.toCinemaItem() }
        else -> people.flatMap { it.results }.map { CinemaItem(it.id, it.name, it.profile_path, "person") }
    }
    val category = when (kind) { "movie" -> "películas"; "tv" -> "series"; else -> "personas" }
    val tab = when (kind) { "movie" -> AppScreen.FilmScreen.route; "tv" -> AppScreen.SerieScreen.route; else -> AppScreen.PeopleScreen.route }
    CinemaScaffold(navController, tab, "Buscar $category", back = true) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            OutlinedTextField(
                value = query, onValueChange = { query = it }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                placeholder = { Text("Busca entre tus $category favoritas") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = CinemaAccent) },
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Clear, "Borrar búsqueda") }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CinemaAccent, unfocusedBorderColor = CinemaOutline,
                    focusedContainerColor = CinemaSurface, unfocusedContainerColor = CinemaSurface
                )
            )
            when {
                query.isBlank() -> EmptyState("Encuentra tu próxima historia", "Escribe un título o un nombre para empezar a explorar.")
                loading -> Progress()
                error != null -> EmptyState("No se ha podido buscar", "Comprueba tu conexión y vuelve a intentarlo.")
                items.isEmpty() -> EmptyState("Sin resultados", "Prueba con otro título o escribe menos palabras.")
                else -> {
                    Text("${items.size} resultados", Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge, color = CinemaMuted)
                    LazyColumn(contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp,
                        bottom = 20.dp + LocalCinemaNavigationClearance.current), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(items, key = { it.kind + it.id }) { item ->
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CinemaSurface)
                                .clickable { navController.navigate(item.route) }.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(imageUrl(item.image), item.title, Modifier.width(64.dp).height(96.dp)
                                    .clip(RoundedCornerShape(10.dp)).background(CinemaElevated), contentScale = ContentScale.Crop)
                                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                    Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(listOfNotNull(item.kindLabel, item.date?.take(4)).joinToString(" · "),
                                        Modifier.padding(top = 6.dp, bottom = 6.dp), style = MaterialTheme.typography.bodyMedium, color = CinemaMuted)
                                    if (kind != "person") RatingBadge(item.rating)
                                }
                                Icon(Icons.Default.KeyboardArrowRight, null, tint = CinemaMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
