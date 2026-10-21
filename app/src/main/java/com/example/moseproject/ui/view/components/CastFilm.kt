package com.example.moseproject.ui.view.components

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.ui.viewmodel.CreditsFilmViewModel

@Composable
fun Cast(navController: NavController, id: String, screenType: ScreenType) {
    val model: CreditsFilmViewModel = viewModel()
    LaunchedEffect(id, screenType) {
        if (screenType == ScreenType.FILM) model.getFilmById(id)
        else model.getSerieCreditsById(id)
    }
    val credits by model.credits.collectAsState()
    val cast = credits?.cast.orEmpty()
    val crew = credits?.crew.orEmpty().distinctBy { it.id }.take(12)
    if (cast.isNotEmpty()) {
        SectionHeading("Reparto principal")
        CreditsRowList(navController, cast.map { it.id.toString() },
            cast.map { imageUrl(it.profile_path).orEmpty() }, cast.map { it.name }, cast.map { it.character.orEmpty() })
    }
    if (crew.isNotEmpty()) {
        SectionHeading("Detrás de las cámaras")
        CreditsRowList(navController, crew.map { it.id.toString() },
            crew.map { imageUrl(it.profile_path).orEmpty() }, crew.map { it.name }, crew.map { it.job.orEmpty() })
    }
}
