package com.example.moseproject.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CarouselCard(list: List<String>){

    val pagerState = rememberPagerState(initialPage = 0)

    val slideUrls = list.take(5)

    val scope = rememberCoroutineScope()


    Column (modifier = Modifier
        .fillMaxSize()
    ){

        HorizontalPager(
            count = slideUrls.size,
            state = pagerState,
            //contentPadding = PaddingValues(horizontal = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .size(500.dp)

        ) { page ->
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .graphicsLayer {
                        val parOffset = calculateCurrentOffsetForPage(page).absoluteValue
                        lerp(
                            start = 0.50f,
                            stop = 1f,
                            fraction = 1f - parOffset.coerceIn(0f, 1f)
                        )
                            .also { scale ->
                                scaleX = scale
                                scaleY = scale
                            }
                        alpha = lerp(
                            start = 0.50f,
                            stop = 1f,
                            fraction = 1f - parOffset.coerceIn(0f, 1f)
                        )
                    }
                    .size(500.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(slideUrls[page])
                        .crossfade(true)
                        .scale(Scale.FIT)
                        .build(),
                    contentDescription = null,
                    //error = painterResource(id = R.drawable.error_image_generic),
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(1.2f)
                        .drawWithContent {
                            drawContent()
                            val colors = listOf(
                                Color.Transparent,
                                Color.Black,
                                Color.Black,
                                Color.Transparent
                            )
//                                    val brush = Brush.horizontalGradient(
//                                        colors = colors,
//                                        startX = 0f,
//                                        endX = size.width
//                                    )
                            val verticalBrush = Brush.verticalGradient(
                                colors = colors,
                                startY = 0f,
                                endY = size.height
                            )
                            //drawRect(brush = brush, blendMode = BlendMode.DstIn)
                            drawRect(brush = verticalBrush, blendMode = BlendMode.DstIn)
                        }

                )
            }

        }
        Row (
            modifier = Modifier
                .height(25.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            repeat(slideUrls.size){it->
                val color =
                    if (pagerState.currentPage == it) Color.White else Color.Gray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .size(12.dp)
                        .background(color)
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(it)
                            }
                        }
                ){
                }
            }
        }
    }
}