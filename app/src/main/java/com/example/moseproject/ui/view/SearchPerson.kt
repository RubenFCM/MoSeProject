package com.example.moseproject.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchPerson(navController: NavController){

    val searchViewModel : SearchViewModel = viewModel()

    var searchQuery by remember { mutableStateOf("") }
    LaunchedEffect(searchQuery) {
        delay(500) // Retraso de 0.5 segundos
        searchViewModel.getPersonSearch(searchQuery)
    }
    val people by searchViewModel.people.collectAsState(initial = emptyList())

    val imagesPeople = people.map { imageUrl ->
        if (imageUrl.results[0].profile_path != null){
            Constants.BASE_URL_IMAGES_W220 + imageUrl.results[0].profile_path
        }else{
            Constants.IMAGE_NOT_FOUND
        }
    }

    val onSearchTextChanged: (String) -> Unit = { newSearchQuery ->
        searchQuery = newSearchQuery
        searchViewModel.getPersonSearch(newSearchQuery)
    }

    LazyColumn() {
        item {
            TextField(
                value = searchQuery,
                onValueChange = onSearchTextChanged,
                label = {
                    Text(
                        text ="Buscar persona",
                        color = Color.White
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                ),
                leadingIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                trailingIcon = {
                    if(searchQuery.isNotEmpty()){
                        IconButton(onClick = { onSearchTextChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar busqueda",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
        items(people.size){ person ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                color = Color.Black//colorResource(id = R.color.surface_search),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Cargar la imagen de la película desde la URL usando Coil
                    Image(
                        painter =
                        rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(data = imagesPeople[person]).apply(block = fun ImageRequest.Builder.() {
                                }).build()
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { navController.navigate( route = AppScreen.PersonID.route+ "/" + people[person].results[0].id) },
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Mostrar los datos de la película
                    Column {
                        Text(
                            text = people[person].results[0].name ,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Conocido por: ${people[person].results[0].known_for_department}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White)

                    }
                }
            }
        }
    }
}