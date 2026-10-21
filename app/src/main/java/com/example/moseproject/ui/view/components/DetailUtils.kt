package com.example.moseproject.ui.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatCinemaDate(date: String?): String =
    runCatching { LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))) }
        .getOrDefault("No disponible")

@Composable
fun DetailUnavailable(navController: NavController, loading: Boolean) {
    Box(Modifier.fillMaxSize()) {
        if (loading) Progress() else EmptyState("Contenido no disponible", "No hemos podido cargar esta ficha. Comprueba tu conexión.")
        IconButton(onClick = { navController.popBackStack() }, Modifier.statusBarsPadding().padding(12.dp)) {
            Icon(Icons.Default.ArrowBack, "Volver")
        }
    }
}
