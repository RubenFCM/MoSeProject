package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.SerieIDResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SerieIDViewModel : ViewModel() {

    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _serie = MutableStateFlow<SerieIDResult?>(null)

    val serie : StateFlow<SerieIDResult?> get() = _serie

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    fun getSerieById(id:String){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val serieID = service.SearchByIdSerie(id,Constants.API_KEY)
                _serie.value = serieID
            }catch (e : Exception){
                e.printStackTrace()
            }finally {
                _isLoading.value = false
            }
        }
    }
}