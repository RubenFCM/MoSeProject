package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.KnownForResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KnowForViewModel : ViewModel(){
    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _combinedCredits = MutableStateFlow<KnownForResult?>(null)

    val combinedCredits : StateFlow<KnownForResult?> get() = _combinedCredits


    fun getCombinedCredits(id:String){
        viewModelScope.launch {
            try {
                val  credits = service.PersonKnowFor(id,Constants.API_KEY)
                _combinedCredits.value = credits
            }catch (e:Exception){
                e.printStackTrace()
            }            }
    }
}