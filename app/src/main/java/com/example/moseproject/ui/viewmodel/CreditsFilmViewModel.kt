package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.CreditsResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreditsFilmViewModel :ViewModel() {
    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _credits = MutableStateFlow<CreditsResult?>(null)

    val credits : StateFlow<CreditsResult?> get() = _credits

    fun getFilmById(id:String){
        viewModelScope.launch {
            try {
                val result = service.CastFilm(id, Constants.API_KEY)
                _credits.value = result
            } catch (e: Exception) {
                println("Error retrofit")
                e.printStackTrace()
            }
        }
    }

    fun getSerieCreditsById(id:String){
        viewModelScope.launch {
            try {
                val result = service.CastSerie(id, Constants.API_KEY)
                _credits.value = result
            } catch (e: Exception) {
                println("Error retrofit")
                e.printStackTrace()
            }
        }
    }
}