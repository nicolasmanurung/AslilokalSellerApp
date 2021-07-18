package com.aslilokal.mitra.ui.pesanan.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.aslilokal.mitra.model.data.repository.AslilokalRepository
import com.aslilokal.mitra.utils.Resource
import kotlinx.coroutines.Dispatchers

class DetailViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {

    fun getDetailOrder(
        tokenKey: String,
        orderId: String
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = mainRepository.getDetailOrder(tokenKey, orderId)
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }
}