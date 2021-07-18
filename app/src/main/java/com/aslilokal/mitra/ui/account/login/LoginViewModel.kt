package com.aslilokal.mitra.ui.account.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.aslilokal.mitra.model.data.repository.AslilokalRepository
import com.aslilokal.mitra.model.remote.request.LoginRequest
import com.aslilokal.mitra.utils.Resource
import kotlinx.coroutines.Dispatchers

class LoginViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {

    fun postLogin(sellerData: LoginRequest) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.postLogin(sellerData)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }
}