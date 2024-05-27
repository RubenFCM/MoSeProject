package com.example.moseproject.ui.view.components

import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.moseproject.data.utils.Constants

@Composable
fun ImageOnImage(image : String){

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data( Constants.BASE_URL_IMAGES_W220 + image )
                .crossfade(true)
                .scale(Scale.FIT)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .absoluteOffset(x = 260.dp, y = 190.dp)
                .clip(shape = RoundedCornerShape(25.dp))
                .height(190.dp)
                .width(130.dp)
                .scale(1f)
        )


}