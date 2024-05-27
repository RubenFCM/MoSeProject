package com.example.moseproject.ui.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.data.utils.Constants

@Composable
fun BackImage(image: String){
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(Constants.BASE_URL_IMAGES_W500 + image )
            .crossfade(true)
            .scale(Scale.FIT)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .height(220.dp)
            .fillMaxWidth()
            .scale(1f)
    )
}