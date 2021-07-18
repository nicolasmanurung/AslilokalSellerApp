package com.aslilokal.mitra.ui.kelola.tambah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.aslilokal.mitra.model.data.repository.AslilokalRepository
import com.aslilokal.mitra.utils.Resource
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody

class TambahProductViewModel(private var mainRepository: AslilokalRepository) : ViewModel() {

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