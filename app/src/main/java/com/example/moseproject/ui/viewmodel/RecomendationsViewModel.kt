package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.FilmResult
import com.example.moseproject.data.model.SerieResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecomendationsViewModel: ViewModel() {
    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _films = MutableStateFlow<List<FilmResult>>(emptyList())
    val films : StateFlow<List<FilmResult>> get() = _films

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun getRecomendationsFilm(id:String){
        _isLoading.value = true
        try {
            viewModelScope.launch {
                val films = service.recommendationsFilm(id,Constants.API_KEY)
                val filmResults = films.results.map { result ->
                    FilmResult(
                        page = films.page,
                        results = listOf(result),
                        total_pages = films.total_pages,
                        total_results = films.total_results
                    )
                }
                _films.value = filmResults
            }
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            _isLoading.value = false
        }
    }

    private val _series = MutableStateFlow<List<SerieResult>>(emptyList())
    val series : StateFlow<List<SerieResult>> get() = _series

    fun getRecomendationsSerie(id:String){
        viewModelScope.launch {
            val series = service.recommendationsSerie(id,Constants.API_KEY)
            val seriesResults = series.results.map { result ->
                SerieResult(
                    page = series.page,
                    results = listOf(result),
                    total_pages = series.total_pages,
                    total_results = series.total_results
                )
            }
            _series.value = seriesResults
        }

    }
}