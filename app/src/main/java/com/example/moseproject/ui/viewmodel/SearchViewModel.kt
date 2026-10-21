package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.*
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SearchViewModel : ViewModel() {
    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()
    private val _films = MutableStateFlow<List<FilmResult>>(emptyList())
    val films: StateFlow<List<FilmResult>> = _films
    private val _series = MutableStateFlow<List<SerieResult>>(emptyList())
    val series: StateFlow<List<SerieResult>> = _series
    private val _people = MutableStateFlow<List<SearchPersonResult>>(emptyList())
    val people: StateFlow<List<SearchPersonResult>> = _people
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private var searchJob: Job? = null

    fun clearSearch() {
        searchJob?.cancel()
        _films.value = emptyList()
        _series.value = emptyList()
        _people.value = emptyList()
        _isLoading.value = false
        _error.value = null
    }

    private fun search(request: suspend () -> Unit) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                request()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se ha podido completar la búsqueda"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFilmsSearch(title: String) = search {
        val result = service.searchFilm(title, Constants.API_KEY)
        _films.value = result.results.map { result.copy(results = listOf(it)) }
    }
    fun getSeriesSearch(title: String) = search {
        val result = service.searchSerie(title, Constants.API_KEY)
        _series.value = result.results.map { result.copy(results = listOf(it)) }
    }
    fun getPersonSearch(title: String) = search {
        val result = service.searchPersonAll(title, Constants.API_KEY)
        _people.value = result.results.map { result.copy(results = listOf(it)) }
    }
}
