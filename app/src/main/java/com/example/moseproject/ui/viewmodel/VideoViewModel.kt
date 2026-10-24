package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.VideoResult
import com.example.moseproject.data.model.spanishYouTubeVideos
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

class VideoViewModel : ViewModel(){

    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _video = MutableStateFlow<VideoResult?>(null)

    val video : StateFlow<VideoResult?> get() = _video

    private val _isLoadingVideo = MutableStateFlow(true)
    val isLoadingVideo: StateFlow<Boolean> get() = _isLoadingVideo
    private var videoRequest: Job? = null

    fun getVideoFilmById(id:String){
        loadVideo { service.VideoMovie(id, Constants.API_KEY) }
    }
    fun getVideoSerieById(id:String){
        loadVideo { service.VideoSerie(id, Constants.API_KEY) }
    }

    private fun loadVideo(request: suspend () -> VideoResult) {
        videoRequest?.cancel()
        _video.value = null
        _isLoadingVideo.value = true
        videoRequest = viewModelScope.launch {
            try {
                val result = request().spanishYouTubeVideos()
                _video.value = result.takeIf { it.results.isNotEmpty() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _video.value = null
                println("Error retrofit")
                e.printStackTrace()
            }finally {
                _isLoadingVideo.value = false
            }
        }
    }
}
