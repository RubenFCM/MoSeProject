package com.example.moseproject.ui.view


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moseproject.data.utils.Constants
import com.example.moseproject.data.utils.ScreenType
import com.example.moseproject.ui.view.components.CreditsRowList
import com.example.moseproject.ui.viewmodel.CreditsFilmViewModel

@Composable
fun Cast(navController: NavController,id :String, screenType: ScreenType){

    val creditsFilmSerieViewModel : CreditsFilmViewModel = viewModel()

    if (screenType == ScreenType.FILM){
        creditsFilmSerieViewModel.getFilmById(id)
    }

    if (screenType == ScreenType.SERIES){
        creditsFilmSerieViewModel.getSerieCreditsById(id)
    }


    val credits = creditsFilmSerieViewModel.credits.collectAsState()

    val imagesCast  = credits.value?.cast?.map { cast ->
        if (cast.profile_path != null){
            Constants.BASE_URL_CREDITS_IMAGE+cast.profile_path
        }
        else{
            Constants.IMAGE_NOT_FOUND_CREDITS
        }
    }
    val idCast = credits.value?.cast?.map { it.id.toString() }.orEmpty()

    val names = credits.value?.cast?.map { cast ->
        cast.name
    }

    val pjs = credits.value?.cast?.map { cast ->
        cast.character
    }

    val imagesCrew = credits.value?.crew?.map { crew ->
        if (crew.profile_path != null){
            Constants.BASE_URL_CREDITS_IMAGE+crew.profile_path
        }
        else{
            Constants.IMAGE_NOT_FOUND_CREDITS
        }
    }?.take(12)

    val namesCrew = credits.value?.crew?.map { crew ->
        crew.name
    }?.take(12)

    val jobs = credits.value?.crew?.map { crew ->
        crew.job
    }?.take(12)

    val idCrew = credits.value?.crew?.map { it.id.toString() }.orEmpty()


    Column {
        if (!imagesCast.isNullOrEmpty() && !names.isNullOrEmpty() && !pjs.isNullOrEmpty()) {
            Text(text = "Reparto:", modifier = Modifier.padding(bottom = 4.dp), color = Color.White)
            CreditsRowList(navController = navController,ids= idCast,images = imagesCast, names = names, pjs = pjs)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (!imagesCrew.isNullOrEmpty() && !namesCrew.isNullOrEmpty() && !jobs.isNullOrEmpty()){
            Text(text = "Dirección:",modifier = Modifier.padding(bottom = 4.dp), color = Color.White)
            CreditsRowList(navController = navController,ids = idCrew ,images = imagesCrew, names = namesCrew, pjs = jobs )
        }
    }
}

