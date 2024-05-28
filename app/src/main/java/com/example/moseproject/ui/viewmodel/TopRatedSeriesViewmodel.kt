package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.ResultSerie
import com.example.moseproject.data.model.SerieResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TopRatedSeriesViewmodel:ViewModel() {
    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _series = MutableStateFlow<List<SerieResult>>(emptyList())
    val series : StateFlow<List<SerieResult>> get() = _series
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    // Almacenar lista de peliculas Próximos estrenos, ordenada por fecha de estreno

    fun getSeriesTopRated(){
        viewModelScope.launch {
            val seriesToday = service.listTopRatedSeries(Constants.API_KEY,1)
            val seriesResult = seriesToday.results.map { result ->
                SerieResult(
                    page = seriesToday.page,
                    results = listOf(result),
                    total_pages = seriesToday.total_pages,
                    total_results = seriesToday.total_results
                )
            }
            _series.value = seriesResult
        }
    }

    private val _seriesPlus = MutableStateFlow<List<ResultSerie>>(emptyList())
    val  seriesPlus : StateFlow<List<ResultSerie>> get() = _seriesPlus

    fun getSeriesTopRatedPlus(){
        _isLoading.value = true
        viewModelScope.launch {
            val allSeries = mutableListOf<ResultSerie>()
            try {
                for (page in 1..12){
                    val  response = service.listTopRatedSeries(Constants.API_KEY, page)
                    val validData = response.results.filter { res ->
                        !res.name.isNullOrEmpty() && !res.poster_path.isNullOrEmpty()
                    }
                    if (validData.isNotEmpty()) {
                        allSeries.addAll(validData)
                    }
                }
                _seriesPlus.value = allSeries
            }catch (e: Exception){
                e.printStackTrace()
            }finally {
                _isLoading.value = false
            }
        }
    }
}