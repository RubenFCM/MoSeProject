package com.example.moseproject.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moseproject.data.RetrofitServiceFactory
import com.example.moseproject.data.model.ResultPeople
import com.example.moseproject.data.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PeopleViewModel :ViewModel(){

    private val service = RetrofitServiceFactory.makeRetrofitServiceFactory()

    private val _listPeople = MutableStateFlow<List<ResultPeople>>(emptyList())
    val listPeople: StateFlow<List<ResultPeople>> get() = _listPeople


    fun getPeople(){
        viewModelScope.launch {

            val allPeople = mutableListOf<ResultPeople>()
            try {
                for (page in 1..12){
                    val  response = service.listPeople(Constants.API_KEY, page)
                    val validPeople = response.results.filter { person ->
                        !person.name.isNullOrEmpty() && !person.profile_path.isNullOrEmpty()
                    }
                    if (validPeople.isNotEmpty()) {
                        allPeople.addAll(validPeople)
                    }
                }
                _listPeople.value = allPeople
            }catch (e: Exception){

                e.printStackTrace()
            }
        }
    }
}