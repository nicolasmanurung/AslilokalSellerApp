package com.kodelapo.mitra.ui.profil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.kodelapo.mitra.model.data.repository.KodelapoRepository
import com.kodelapo.mitra.utils.Resource
import kotlinx.coroutines.Dispatchers

class ProfilViewModel(private val mainRepository: KodelapoRepository) : ViewModel() {
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