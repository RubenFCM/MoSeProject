package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.All
import com.example.moseproject.data.model.ResultSerie
import com.example.moseproject.data.model.SerieResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrendingSerieViewModel :ViewModel() {

    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    // Almacenar lista de caratulas Populares
    private val _listSeries = MutableStateFlow<List<SerieResult>>(emptyList())
    val listSeries : StateFlow<List<SerieResult>> get() = _listSeries

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    fun getSeriesTrending(){
        viewModelScope.launch {
            val series = service.listTrendigSeries(Constants.API_KEY,1)
            val allResult = series.results.map { result ->
                SerieResult(
                    page = series.page,
                    results = listOf(result),
                    total_pages = series.total_pages,
                    total_results = series.total_results
                )
            }
            _listSeries.value = allResult
        }
    }

    private val _seriesPlus = MutableStateFlow<List<ResultSerie>>(emptyList())
    val  seriesPlus : StateFlow<List<ResultSerie>> get() = _seriesPlus

    fun getSeriesTrendingPlus(){
        _isLoading.value = true
        viewModelScope.launch {
            val allSeries = mutableListOf<ResultSerie>()
            try {
                for (page in 1..12){
                    val  response = service.listTrendigSeries(Constants.API_KEY, page)
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