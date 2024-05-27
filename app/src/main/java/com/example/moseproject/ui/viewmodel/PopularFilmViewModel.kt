package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.FilmResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PopularFilmViewModel :ViewModel(){

    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _films = MutableStateFlow<List<FilmResult>>(emptyList())
    val films : StateFlow<List<FilmResult>> get() = _films

    // Almacenar lista de caratulas Populares
    private val _listImages = MutableStateFlow<List<String>>(emptyList())
    val listImages : StateFlow<List<String>> get() = _listImages

    fun getImagesFilmsPopular(){
        viewModelScope.launch {
            val films = service.listPopularMovies(Constants.API_KEY)
            val img = films.results.map { Constants.BASE_URL_IMAGES_W500 + it.poster_path }
            _listImages.value = img
        }
    }
}