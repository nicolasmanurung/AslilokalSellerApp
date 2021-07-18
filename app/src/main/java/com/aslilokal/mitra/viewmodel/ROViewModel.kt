package com.aslilokal.mitra.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.mitra.model.data.repository.AslilokalRepository
import com.aslilokal.mitra.model.remote.response.ROCityResponse
import com.aslilokal.mitra.utils.ResourcePagination
import kotlinx.coroutines.launch
import java.io.IOException

class ROViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val citiesResults: MutableLiveData<ResourcePagination<ROCityResponse>> = MutableLiveData()
    var citiesResponse: ROCityResponse? = null

    suspend fun getCitiesByRO(key: String) = viewModelScope.launch {
        citiesResults.postValue(ResourcePagination.Loading())
        try {
            val response = mainRepository.getCitiesRO(key)
            if (response.body()?.rajaongkir?.status?.code == 200) {
                response.body()?.let { citiesResult ->
                    if (citiesResponse == null) {
                        citiesResponse = citiesResult
                    }
                    citiesResults.postValue(
                        ResourcePagination.Success(
                            citiesResponse ?: citiesResult
                        )
                    )
                }
            } else {
                citiesResults.postValue(ResourcePagination.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> citiesResults.postValue(ResourcePagination.Error("Jaringan lemah"))
                else -> citiesResults.postValue(ResourcePagination.Error("Kesalahan tak terduga"))
            }
        }
    }
}