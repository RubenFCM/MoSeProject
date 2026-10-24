package com.example.moseproject.ui.view.components

import android.annotation.SuppressLint
import androidx.compose.animation.Animatable as ColorAnimatable
import androidx.compose.animation.core.Animatable as FloatAnimatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moseproject.R
import com.example.moseproject.navigation.AppScreen
import com.example.moseproject.ui.theme.CinemaNavigationActive
import com.example.moseproject.ui.theme.CinemaNavigationBackground
import com.example.moseproject.ui.theme.CinemaNavigationInactive
import com.example.moseproject.ui.theme.CinemaNavigationSelection
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private data class NavigationDestination(val route: String, val label: String, val icon: Int)

private val destinations = listOf(
    NavigationDestination(AppScreen.HomeScreen.route, "Inicio", R.drawable.ic_lateral_home),
    NavigationDestination(AppScreen.FilmScreen.route, "Películas", R.drawable.ic_lateral_film),
    NavigationDestination(AppScreen.SerieScreen.route, "Series", R.drawable.ic_lateral_serie),
    NavigationDestination(AppScreen.PeopleScreen.route, "Personas", R.drawable.ic_people)
)

private const val SelectionDurationMillis = 250

private class NavigationAnimationState(
    initialIndex: Int,
    private val activeColor: Color,
    private val inactiveColor: Color
) {
    val position = FloatAnimatable(initialIndex.toFloat())
    val colors = List(destinations.size) { index ->
        ColorAnimatable(if (index == initialIndex) activeColor else inactiveColor)
    }

    suspend fun select(index: Int) = coroutineScope {
        launch {
            position.animateTo(index.toFloat(), tween(SelectionDurationMillis, easing = FastOutSlowInEasing))
        }
        colors.forEachIndexed { itemIndex, color ->
            launch {
                color.animateTo(
                    if (itemIndex == index) activeColor else inactiveColor,
                    tween(SelectionDurationMillis, easing = FastOutSlowInEasing)
                )
            }
        }
    }
}

private val LocalNavigationAnimation = staticCompositionLocalOf<NavigationAnimationState?> { null }

// Keep the visual transition alive when NavHost replaces one screen with another.
@Composable
fun CinemaNavigationAnimation(content: @Composable () -> Unit) {
    val activeColor = CinemaNavigationActive
    val inactiveColor = CinemaNavigationInactive
    val animation = remember(activeColor, inactiveColor) {
        NavigationAnimationState(0, activeColor, inactiveColor)
    }
    CompositionLocalProvider(LocalNavigationAnimation provides animation, content = content)
}


@Composable
fun CinemaBottomNavigation(
    selectedRoute: String,
    onDestinationSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = destinations.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)
    val activeColor = CinemaNavigationActive
    val inactiveColor = CinemaNavigationInactive
    val animation = LocalNavigationAnimation.current ?: remember(activeColor, inactiveColor) {
        NavigationAnimationState(selectedIndex, activeColor, inactiveColor)
    }

    LaunchedEffect(selectedIndex, animation) { animation.select(selectedIndex) }

    Box(
        modifier.fillMaxWidth().navigationBarsPadding().padding(top = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth(.82f).clip(CircleShape)
                .background(CinemaNavigationBackground).padding(2.dp)
        ) {
            val itemWidth = this.maxWidth / destinations.size

            // A single moving background spans both the icon and its label.
            Box(Modifier.matchParentSize()) {
                Box(
                    Modifier.offset(x = itemWidth * animation.position.value)
                        .width(itemWidth).fillMaxHeight().padding(horizontal = 2.dp)
                        .clip(CircleShape).background(CinemaNavigationSelection)
                )
            }

            Row(
                Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    .height(IntrinsicSize.Min).selectableGroup()
            ) {
                destinations.forEachIndexed { index, destination ->
                    val color = animation.colors[index].value
                    Column(
                        Modifier.weight(1f).fillMaxHeight().clip(CircleShape)
                            .selectable(
                                selected = selectedIndex == index,
                                role = Role.Tab,
                                onClick = { onDestinationSelected(destination.route) }
                            )
                            .padding(horizontal = 2.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(painterResource(destination.icon), null, Modifier.size(22.dp), tint = color)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            destination.label,
                            color = color,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
