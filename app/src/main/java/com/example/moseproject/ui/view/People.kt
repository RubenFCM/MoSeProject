package com.example.moseproject.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.view.components.*
import com.example.moseproject.ui.viewmodel.PeopleViewModel

@Composable
fun People(navController: NavController) {
    val model: PeopleViewModel = viewModel()
    LaunchedEffect(Unit) { model.getPeople() }
    val people by model.listPeople.collectAsState()
    val loading by model.isLoading.collectAsState()
    CinemaScaffold(navController, AppScreen.PeopleScreen.route, "Personas", AppScreen.SearchPerson.route) { padding ->
        if (loading) Progress(Modifier.padding(padding))
        else if (people.isEmpty()) EmptyState("Sin personas disponibles", "Comprueba tu conexión y vuelve a intentarlo.", Modifier.padding(padding))
        else LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp), modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp + LocalCinemaNavigationClearance.current),
            horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ScreenIntro("Delante y detrás de la cámara", "Talento que\ntraspasa la pantalla.", "Conoce a quienes dan vida a tus historias favoritas.", horizontalPadding = 0.dp)
            }
            items(people.distinctBy { it.id }, key = { it.id }) { PosterCard(it.toCinemaItem(), navController) }
        }
    }
}
