package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.FilmResult
import com.example.moseproject.data.model.Result
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TopRatedFilmViewModel :ViewModel() {

    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _films = MutableStateFlow<List<FilmResult>>(emptyList())
    val films : StateFlow<List<FilmResult>> get() = _films

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    fun getFilmsTopRated(){
        viewModelScope.launch {
            val filmsTopRated = service.listTopRatedFilms(Constants.API_KEY,1)
            val filmResults = filmsTopRated.results.map { result ->
                FilmResult(
                    page = filmsTopRated.page,
                    results = listOf(result),
                    total_pages = filmsTopRated.total_pages,
                    total_results = filmsTopRated.total_results
                )
            }
            _films.value = filmResults
        }

    }

    private val _filmsPlus = MutableStateFlow<List<Result>>(emptyList())
    val  filmsPlus : StateFlow<List<Result>> get() = _filmsPlus

    fun getFilmsTopRatedPlus(){
        _isLoading.value = true
        viewModelScope.launch {
            val allFilms = mutableListOf<Result>()
            try {
                for (page in 1..12){
                    val  response = service.listTopRatedFilms(Constants.API_KEY, page)
                    val validData = response.results.filter { res ->
                        !res.title.isNullOrEmpty() && !res.poster_path.isNullOrEmpty()
                    }
                    if (validData.isNotEmpty()) {
                        allFilms.addAll(validData)
                    }
                }
                _filmsPlus.value = allFilms
            }catch (e: Exception){
                e.printStackTrace()
            }finally {
                _isLoading.value = false
            }
        }
    }
}