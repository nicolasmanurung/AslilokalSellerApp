package com.kodelapo.mitra.ui.kelola.tambah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.kodelapo.mitra.model.data.repository.KodelapoRepository
import com.kodelapo.mitra.utils.Resource
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody

class TambahProductViewModel(private var mainRepository: KodelapoRepository) : ViewModel() {
    fun postProduct(
        token: String,
        imgName: MultipartBody.Part,
        idSellerAccount: RequestBody,
        nameProduct: RequestBody,
        productCategory: RequestBody,
        priceProduct: RequestBody,
        productWeight: RequestBody,
        descProduct: RequestBody,
        isAvailable: RequestBody,
        promoPrice: RequestBody
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = mainRepository.postOneProduct(
                        token,
                        imgName,
                        idSellerAccount,
                        nameProduct,
                        productCategory,
                        priceProduct,
                        productWeight,
                        descProduct,
                        isAvailable,
                        promoPrice
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }
}