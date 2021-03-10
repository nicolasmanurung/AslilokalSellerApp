package com.aslilokal.mitra.ui.kelola.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.aslilokal.mitra.model.data.repository.KodelapoRepository
import com.aslilokal.mitra.model.remote.request.OneProduct
import com.aslilokal.mitra.utils.Resource
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody

class EditViewModel(private var mainRepository: KodelapoRepository) : ViewModel() {

    fun getOneProduct(token: String, idProduct: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getOneProduct(token, idProduct)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }

    fun putOneProduct(
        token: String,
        id: String,
        dataEdit: OneProduct
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = mainRepository.putOneProduct(
                        token,
                        id,
                        dataEdit
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }

    // edit one image
    fun putOneImage(
        token: String,
        imageStream: MultipartBody.Part,
        imgKey: RequestBody
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.putOneImage(token, imageStream, imgKey)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }
}