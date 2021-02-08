package com.kodelapo.mitra.model.data.repository

import com.kodelapo.mitra.model.data.api.ApiHelper
import com.kodelapo.mitra.model.remote.request.LoginRequest
import com.kodelapo.mitra.model.remote.response.LoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class KodelapoRepository(val apiHelper: ApiHelper) {
    suspend fun postLogin(sellerData: LoginRequest): Response<LoginResponse> =
        apiHelper.postLogin(sellerData)

    suspend fun getProduct(token: String, id: String, type: String, page: Int, limit: Int) =
        apiHelper.getProduct(token, id, type, page, limit)

    suspend fun getShopInfo(token: String, id: String) = apiHelper.getShopAccount(token, id)

    suspend fun postOneProduct(
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
    ) = apiHelper.postOneProduct(
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
}