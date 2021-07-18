package com.aslilokal.mitra.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.aslilokal.mitra.model.data.repository.AslilokalRepository
import com.aslilokal.mitra.model.remote.request.RegisterRequest
import com.aslilokal.mitra.utils.Resource
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AccountViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {

    fun postRegister(
        registerRequest: RegisterRequest
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.postRegister(registerRequest)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }

    fun getToken(
        authToken: String,
        verifyToken: String
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getToken(authToken, verifyToken)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }

    fun postResubmit(
        authToken: String
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.postResubmitTokenVerify(authToken)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }

    fun postSellerInfo(
        token: String,
        imgSeller: MultipartBody.Part,
        ktpSeller: MultipartBody.Part,
        idSellerAccount: RequestBody,
        nameSellerBiodata: RequestBody,
        idKtpNumber: RequestBody,
        telpNumber: RequestBody,
        addressSeller: RequestBody,
        birthDateSeller: RequestBody,
        ovoNumber: RequestBody,
        danaNumber: RequestBody,
        gopayNumber: RequestBody
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = mainRepository.postSellerBio(
                        token,
                        imgSeller,
                        ktpSeller,
                        idSellerAccount,
                        nameSellerBiodata,
                        idKtpNumber,
                        telpNumber,
                        addressSeller,
                        birthDateSeller,
                        ovoNumber,
                        danaNumber,
                        gopayNumber
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }

    fun postShopInfo(
        token: String,
        imgShop: MultipartBody.Part,
        idSellerAccount: RequestBody,
        nameShop: RequestBody,
        noTelpSeller: RequestBody,
        noWhatsappShop: RequestBody,
        isPickup: RequestBody,
        isDelivery: RequestBody,
        addressShop: RequestBody,
        postalCodeInput: RequestBody,
        isTwentyFourHours: RequestBody,
        openTime: RequestBody,
        closeTime: RequestBody,
        cityId: RequestBody,
        provinceId: RequestBody,
        province: RequestBody,
        cityName: RequestBody,
        postalCodeRO: RequestBody
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = mainRepository.postShopBio(
                        token,
                        imgShop,
                        idSellerAccount,
                        nameShop,
                        noTelpSeller,
                        noWhatsappShop,
                        isPickup,
                        isDelivery,
                        addressShop,
                        postalCodeInput,
                        isTwentyFourHours,
                        openTime,
                        closeTime,
                        cityId, provinceId, province, cityName, postalCodeRO
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }

    fun putRegistrationShopSubmit(
        token: String,
        idUser: String,
        status: String
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = mainRepository.putRequestShopRegister(
                        token,
                        idUser,
                        status
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Ada kesalahan"))
        }
    }
}