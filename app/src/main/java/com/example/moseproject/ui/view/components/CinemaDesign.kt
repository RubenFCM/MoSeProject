package com.example.moseproject.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.R
import com.example.moseproject.ui.theme.*
import java.util.Locale

data class CinemaItem(
    val id: Int,
    val title: String,
    val image: String?,
    val kind: String,
    val date: String? = null,
    val rating: Double = 0.0,
    val backdrop: String? = null,
    val overview: String? = null
) {
    val route: String get() = when (kind) {
        "movie" -> AppScreen.FilmID.route
        "tv" -> AppScreen.SerieID.route
        else -> AppScreen.PersonID.route
    } + "/" + id
    val kindLabel: String get() = when (kind) { "movie" -> "Película"; "tv" -> "Serie"; else -> "Persona" }
}

fun imageUrl(path: String?, backdrop: Boolean = false): String? =
    path?.takeIf { it.isNotBlank() && it != "null" }?.let {
        if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/" + (if (backdrop) "w780" else "w500") + it
    }

@Composable
fun CinemaScaffold(
    navController: NavController,
    selected: String,
    title: String,
    searchRoute: String? = null,
    back: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = CinemaBackground,
        topBar = {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (back) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = CinemaText)
                    }
                } else {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(CinemaAccent), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = CinemaBackground, modifier = Modifier.size(23.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(if (back) "EXPLORA CINEVERSE" else "CINEVERSE", style = MaterialTheme.typography.labelSmall, color = CinemaAccent)
                    Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (searchRoute != null) {
                    IconButton(onClick = { navController.navigate(searchRoute) }, modifier = Modifier.clip(CircleShape).background(CinemaSurface)) {
                        Icon(Icons.Default.Search, "Buscar", tint = CinemaText)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = CinemaBackground, tonalElevation = 0.dp) {
                val tabs = listOf(
                    Triple(AppScreen.HomeScreen.route, "Inicio", R.drawable.ic_lateral_home),
                    Triple(AppScreen.FilmScreen.route, "Películas", R.drawable.ic_lateral_film),
                    Triple(AppScreen.SerieScreen.route, "Series", R.drawable.ic_lateral_serie),
                    Triple(AppScreen.PeopleScreen.route, "Personas", R.drawable.ic_people)
                )
                tabs.forEach { (route, label, icon) ->
                    NavigationBarItem(
                        selected = selected == route,
                        onClick = {
                            if (selected != route || back) {
                                navController.navigate(route) {
                                    popUpTo(AppScreen.HomeScreen.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(painterResource(icon), null, modifier = Modifier.size(22.dp)) },
                        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CinemaAccent, selectedTextColor = CinemaAccent,
                            indicatorColor = CinemaElevated,
                            unselectedIconColor = CinemaMuted, unselectedTextColor = CinemaMuted
                        )
                    )
                }
            }
        },
        content = content
    )
}

@Composable
fun ScreenIntro(eyebrow: String, title: String, subtitle: String, horizontalPadding: Dp = 20.dp) {
    Column(Modifier.padding(horizontal = horizontalPadding).padding(top = 12.dp, bottom = 24.dp)) {
        Text(eyebrow.uppercase(Locale("es")), style = MaterialTheme.typography.labelSmall, color = CinemaAccent)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.headlineLarge, color = CinemaText)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = CinemaMuted)
    }
}

@Composable
fun SectionHeading(title: String, action: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 26.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = CinemaText)
        if (action != null) TextButton(onClick = action) {
            Text("Ver todo", color = CinemaAccent)
            Icon(Icons.Default.KeyboardArrowRight, null, Modifier.size(18.dp), tint = CinemaAccent)
        }
    }
}

@Composable
fun RatingBadge(rating: Double) {
    Surface(color = CinemaBackground.copy(alpha = .88f), shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.padding(horizontal = 7.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, Modifier.size(12.dp), tint = Color(0xFFFFCF70))
            Spacer(Modifier.width(4.dp))
            Text(if (rating > 0) String.format(Locale("es"), "%.1f", rating) else "—", style = MaterialTheme.typography.labelLarge, color = CinemaText)
        }
    }
}

@Composable
fun PosterCard(item: CinemaItem, navController: NavController, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).clickable { navController.navigate(item.route) }) {
        Box(Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(14.dp)).background(CinemaSurface)) {
            Icon(if (item.kind == "person") Icons.Default.Person else Icons.Default.PlayArrow, null,
                Modifier.align(Alignment.Center).size(32.dp), tint = CinemaMuted)
            AsyncImage(imageUrl(item.image), item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (item.kind != "person") Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) { RatingBadge(item.rating) }
        }
        Text(item.title, Modifier.padding(top = 10.dp, start = 2.dp, end = 2.dp),
            color = CinemaText, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(listOfNotNull(item.kindLabel, item.date?.takeIf { it.length >= 4 }?.take(4)).joinToString(" · "),
            Modifier.padding(top = 3.dp, start = 2.dp, bottom = 4.dp), style = MaterialTheme.typography.bodyMedium, color = CinemaMuted)
    }
}

@Composable
fun PosterRow(items: List<CinemaItem>, navController: NavController) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(items, key = { it.kind + it.id }) { item -> PosterCard(item, navController, Modifier.width(140.dp)) }
    }
}

@Composable
fun PosterGrid(items: List<CinemaItem>, navController: NavController, modifier: Modifier = Modifier) {
    if (items.isEmpty()) {
        EmptyState("Todavía no hay contenido", "Vuelve a intentarlo en unos instantes.", modifier)
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp), modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            items(items, key = { it.kind + it.id }) { item -> PosterCard(item, navController) }
        }
    }
}

@Composable
fun FeaturedCard(item: CinemaItem, navController: NavController, label: String = "EN EL FOCO") {
    Box(Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(350.dp).clip(RoundedCornerShape(24.dp))
        .background(CinemaSurface).clickable { navController.navigate(item.route) }) {
        AsyncImage(imageUrl(item.backdrop ?: item.image, true), item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to Color.Transparent, .35f to CinemaBackground.copy(alpha = .1f), 1f to Color(0xFF08090D))))
        Surface(Modifier.align(Alignment.TopStart).padding(18.dp), color = CinemaBackground.copy(alpha = .8f), shape = RoundedCornerShape(8.dp)) {
            Text(label, Modifier.padding(horizontal = 10.dp, vertical = 7.dp), style = MaterialTheme.typography.labelSmall, color = CinemaText)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingBadge(item.rating)
                Spacer(Modifier.width(10.dp))
                Text(listOfNotNull(item.kindLabel, item.date?.take(4)).joinToString("  ·  "), style = MaterialTheme.typography.bodyMedium, color = CinemaText)
            }
            Spacer(Modifier.height(12.dp))
            Text(item.title, style = MaterialTheme.typography.headlineMedium, color = CinemaText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!item.overview.isNullOrBlank()) Text(item.overview, Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium, color = CinemaMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigate(item.route) }, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Descubrir")
            }
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Search, null, Modifier.size(44.dp), tint = CinemaAccent)
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = CinemaText)
        Text(message, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyMedium, color = CinemaMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun DetailHero(title: String, poster: String?, backdrop: String?, category: String, metadata: String, rating: Double, navController: NavController) {
    Box(Modifier.fillMaxWidth().height(250.dp).background(CinemaSurface)) {
        AsyncImage(imageUrl(backdrop, true), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, CinemaBackground))))
        Surface(Modifier.padding(20.dp).statusBarsPadding(), shape = CircleShape, color = CinemaBackground.copy(alpha = .8f)) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Volver", tint = CinemaText) }
        }
        Text(category.uppercase(Locale("es")), Modifier.align(Alignment.BottomEnd).padding(22.dp),
            style = MaterialTheme.typography.labelSmall, color = CinemaAccent)
    }
    Row(Modifier.padding(horizontal = 20.dp).padding(bottom = 18.dp), verticalAlignment = Alignment.Top) {
        AsyncImage(imageUrl(poster), title, Modifier.width(92.dp).height(138.dp).clip(RoundedCornerShape(14.dp)).background(CinemaSurface), contentScale = ContentScale.Crop)
        Column(Modifier.padding(start = 18.dp).weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = CinemaText)
            Text(metadata, Modifier.padding(top = 8.dp, bottom = 10.dp), style = MaterialTheme.typography.bodyMedium, color = CinemaMuted)
            RatingBadge(rating)
        }
    }
}

@Composable
fun GenreChips(genres: List<String>) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(genres) { genre ->
            Surface(shape = RoundedCornerShape(9.dp), color = CinemaSurface, border = androidx.compose.foundation.BorderStroke(1.dp, CinemaOutline)) {
                Text(genre, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = CinemaMuted, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun OverviewSection(overview: String) {
    var expanded by remember(overview) { mutableStateOf(false) }
    SectionHeading("Sinopsis")
    Text(overview.ifBlank { "Todavía no hay una sinopsis disponible." }, Modifier.padding(horizontal = 20.dp),
        style = MaterialTheme.typography.bodyLarge, color = CinemaMuted,
        maxLines = if (expanded) Int.MAX_VALUE else 4, overflow = TextOverflow.Ellipsis)
    if (overview.length > 220) TextButton(onClick = { expanded = !expanded }, modifier = Modifier.padding(start = 8.dp)) {
        Text(if (expanded) "Leer menos" else "Leer más")
    }
}

@Composable
fun InfoPanel(values: List<Pair<String, String>>) {
    Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CinemaSurface).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        values.forEach { (label, value) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = CinemaMuted)
                Text(value, Modifier.weight(1.2f), style = MaterialTheme.typography.labelLarge, color = CinemaText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        }
    }
}
