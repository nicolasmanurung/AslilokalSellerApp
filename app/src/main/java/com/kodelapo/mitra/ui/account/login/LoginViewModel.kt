package com.kodelapo.mitra.ui.account.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.kodelapo.mitra.model.data.repository.KodelapoRepository
import com.kodelapo.mitra.model.remote.request.LoginRequest
import com.kodelapo.mitra.utils.Resource
import kotlinx.coroutines.Dispatchers

class LoginViewModel(private val mainRepository: KodelapoRepository) : ViewModel() {

    fun postLogin(sellerData: LoginRequest) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.postLogin(sellerData)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }
}