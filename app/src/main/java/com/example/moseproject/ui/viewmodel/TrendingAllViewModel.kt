package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.All
import com.example.moseproject.data.model.ResultAll
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrendingAllViewModel :ViewModel() {

    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _all = MutableStateFlow<List<All>>(emptyList())
    val  all : StateFlow<List<All>> get() = _all
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    fun getAllTrendingWeek(){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val allTrending = service.listTendringAllWeek(Constants.API_KEY,1)
                val allResult = allTrending.results.map { result ->
                    All(
                        page = allTrending.page,
                        results = listOf(result),
                        total_pages = allTrending.total_pages,
                        total_results = allTrending.total_results
                    )
                }
                _all.value = allResult
            }catch (e :Exception){
                e.printStackTrace()
            }finally {
                _isLoading.value = false
            }
        }
    }

    private val _allPlus = MutableStateFlow<List<ResultAll>>(emptyList())
    val  allPlus : StateFlow<List<ResultAll>> get() = _allPlus


    fun getAllTrendingWeekPlus(){
        _isLoading.value = true
        viewModelScope.launch {
            val allFilmsSeries = mutableListOf<ResultAll>()
            try {
                for (page in 1..12){
                    val  response = service.listTendringAllWeek(Constants.API_KEY, page)
                    val validData = response.results.filter { res ->
                        (!res.title.isNullOrEmpty() || !res.name.isNullOrEmpty()) && !res.poster_path.isNullOrEmpty()
                    }
                    if (validData.isNotEmpty()) {
                        allFilmsSeries.addAll(validData)
                    }
                }
                _allPlus.value = allFilmsSeries
            }catch (e: Exception){

                e.printStackTrace()
            }finally {
                _isLoading.value = false
            }
        }
    }
}