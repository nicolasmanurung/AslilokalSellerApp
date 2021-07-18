package com.aslilokal.mitra.ui.profil

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.aslilokal.mitra.model.data.repository.AslilokalRepository
import com.aslilokal.mitra.model.remote.response.Shop
import com.aslilokal.mitra.model.remote.response.StatusResponse
import com.aslilokal.mitra.utils.Resource
import com.aslilokal.mitra.utils.ResourcePagination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class ProfilViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val putShopResults: MutableLiveData<ResourcePagination<StatusResponse>> = MutableLiveData()
    var putShopResponse: StatusResponse? = null

    suspend fun putShopInfo(
        token: String,
        username: String,
        shopData: Shop
    ) = viewModelScope.launch {
        putShopResults.postValue(ResourcePagination.Loading())
        try {
            val response = mainRepository.putShopInfo(token, username, shopData)
            if (response.body()?.success == true) {
                response.body()?.let { result ->
                    if (putShopResponse == null) {
                        putShopResponse = result
                    }
                    putShopResults.postValue(ResourcePagination.Success(putShopResponse ?: result))
                }
            } else {
                putShopResults.postValue(ResourcePagination.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> putShopResults.postValue(ResourcePagination.Error("Jaringan lemah"))
                else -> putShopResults.postValue(ResourcePagination.Error("Kesalahan tak terduga"))
            }
        }
    }

    fun getProfile(token: String, idSellerAccount: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = mainRepository.getShopInfo(
                        token,
                        idSellerAccount
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }
}