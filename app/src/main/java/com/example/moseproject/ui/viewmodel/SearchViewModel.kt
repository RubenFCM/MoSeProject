package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.FilmResult
import com.example.moseproject.data.model.SearchPersonResult
import com.example.moseproject.data.model.SerieResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel:ViewModel() {
    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _films = MutableStateFlow<List<FilmResult>>(emptyList())
    val films : StateFlow<List<FilmResult>> get() = _films

    // Buscar pelicula por título

    fun getFilmsSearch(title:String){
        viewModelScope.launch {
            val filmsSearch = service.searchFilm(title,Constants.API_KEY)
            val filmResults = filmsSearch.results.map { result ->
                FilmResult(
                    page = filmsSearch.page,
                    results = listOf(result),
                    total_pages = filmsSearch.total_pages,
                    total_results = filmsSearch.total_results
                )
            }
            _films.value = filmResults
        }
    }

    private val _series = MutableStateFlow<List<SerieResult>>(emptyList())
    val series : StateFlow<List<SerieResult>> get() = _series

    // Buscar serie por título
    fun getSeriesSearch(title:String){
        viewModelScope.launch {
            val serieSearch = service.searchSerie(title,Constants.API_KEY)
            val serieResults = serieSearch.results.map { result ->
                SerieResult(
                    page = serieSearch.page,
                    results = listOf(result),
                    total_pages = serieSearch.total_pages,
                    total_results = serieSearch.total_results
                )
            }
            _series.value = serieResults
        }
    }

    private val _people = MutableStateFlow<List<SearchPersonResult>>(emptyList())
    val people : StateFlow<List<SearchPersonResult>> get() = _people

    // Buscar persona por nombre
    fun getPersonSearch(title: String){
        viewModelScope.launch {
            val personSearch = service.searchPersonAll(title,Constants.API_KEY)
            val personResult = personSearch.results.map { result ->
                SearchPersonResult(
                    page = personSearch.page,
                    results = listOf(result),
                    total_pages = personSearch.total_pages,
                    total_results = personSearch.total_results
                )
            }
            _people.value = personResult
        }
    }
}
