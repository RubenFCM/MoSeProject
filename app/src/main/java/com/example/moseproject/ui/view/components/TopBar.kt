package com.example.moseproject.ui.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.navigation.AppScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Topbar(title:String, navController: NavController,screenType: ScreenType,drawerState: DrawerState?, scope: CoroutineScope?){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
        ),
        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                scope?.launch {
                    drawerState?.open()
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Localized description",
                    tint = Color.White
                )
            }
        },

        actions = {
            if (screenType == ScreenType.HOME){
                IconButton(onClick = { exitProcess(1) }) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp ,
                        contentDescription ="Salir",
                        tint = Color.White )
                }
            }else{
                IconButton(onClick = {
                    when (screenType){
                        ScreenType.FILM ->{
                            navController.navigate( route = AppScreen.SearhScreenFilm.route )
                        }
                        ScreenType.SERIES ->{
                            navController.navigate( route = AppScreen.SearhScreenSerie.route )
                        }
                        ScreenType.PEOPLE ->{
                            navController.navigate(route = AppScreen.SearchPerson.route)
                        }
                        ScreenType.HOME -> TODO()
                    }

                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Localized description",
                        tint = Color.White
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )

}



