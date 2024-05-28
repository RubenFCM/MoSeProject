package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.FilmIdResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FilmIdViewModel :ViewModel() {

    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _film = MutableStateFlow<FilmIdResult?>(null)

    val film : StateFlow<FilmIdResult?> get() = _film

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    fun getFilmById(id:String){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val filmID = service.SearchByIdFilm(id,Constants.API_KEY)
                _film.value = filmID
            } catch (e: Exception) {
                e.printStackTrace()
                _film.value = null
            }finally {
                _isLoading.value = false
            }
        }
    }
}