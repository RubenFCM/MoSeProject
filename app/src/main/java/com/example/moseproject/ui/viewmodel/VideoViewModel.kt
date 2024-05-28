package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.VideoResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VideoViewModel : ViewModel(){

    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _video = MutableStateFlow<VideoResult?>(null)

    val video : StateFlow<VideoResult?> get() = _video

    private val _isLoadingVideo = MutableStateFlow(true)
    val isLoadingVideo: StateFlow<Boolean> get() = _isLoadingVideo
    fun getVideoFilmById(id:String){
        _isLoadingVideo.value = true
        viewModelScope.launch {
            try {
                val result = service.VideoMovie(id, Constants.API_KEY)
                if (result.results.size == 0){
                    null
                }else{
                    _video.value = result
                }
            } catch (e: Exception) {
                println("Error retrofit")
                e.printStackTrace()
            }finally {
                _isLoadingVideo.value = false
            }
        }
    }
    fun getVideoSerieById(id:String){
        _isLoadingVideo.value = true
        viewModelScope.launch {
            try {
                val result = service.VideoSerie(id, Constants.API_KEY)
                if (result.results.size == 0){
                    null
                }else{
                    _video.value = result
                }
            } catch (e: Exception) {
                println("Error retrofit")
                e.printStackTrace()
            }finally {
                _isLoadingVideo.value = false
            }
        }
    }
}