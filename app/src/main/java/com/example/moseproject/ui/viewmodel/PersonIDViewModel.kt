package com.example.moseproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.PersonResult
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PersonIDViewModel: ViewModel() {
    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _dataPerson = MutableStateFlow<PersonResult?>(null)

    val dataPerson : StateFlow<PersonResult?> get() = _dataPerson

    fun getPerson(id:String){
        viewModelScope.launch {
            try {
                val person = service.searchPerson(id,Constants.API_KEY)
                _dataPerson.value = person
            }catch (e: Exception){
                e.printStackTrace()
                _dataPerson.value = null
            }
        }
    }
}