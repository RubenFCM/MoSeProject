package com.example.moseproject.ui.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.moseproject.data.model.Network
import com.example.moseproject.data.model.ProductionCompany
import com.example.moseproject.ui.theme.*

@Composable
fun Companies(companies: List<ProductionCompany>) {
    OrganizationList("Producción", companies.map { it.name to it.logo_path })
}

@Composable
fun Networks(net: List<Network>) {
    OrganizationList("Dónde se emite", net.map { it.name to it.logo_path })
}

@Composable
private fun OrganizationList(title: String, organizations: List<Pair<String, String?>>) {
    var expanded by remember(organizations) { mutableStateOf(false) }
    SectionHeading(title)
    Surface(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = CinemaSurface) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            (if (expanded) organizations else organizations.take(3)).forEach { (name, logo) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = CinemaText)
                    if (!logo.isNullOrBlank()) ShowIcon(logo)
                }
            }
            if (organizations.size > 3) TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Ver menos" else "Ver todas")
            }
        }
    }
}

@Composable
fun ShowIcon(icon: String?) {
    Surface(shape = RoundedCornerShape(6.dp), color = CinemaLogoBackground) {
        AsyncImage(imageUrl(icon), null, Modifier.width(60.dp).height(30.dp).padding(5.dp))
    }
}
