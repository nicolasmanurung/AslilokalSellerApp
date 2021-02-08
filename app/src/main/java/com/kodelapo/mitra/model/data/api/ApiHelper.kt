package com.kodelapo.mitra.model.data.api

import com.kodelapo.mitra.model.remote.request.LoginRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ApiHelper(private val apiService: KodelapoAPI) {
    suspend fun postLogin(sellerData: LoginRequest) = apiService.postLoginSeller(sellerData)
    suspend fun getProduct(token: String, id: String, type: String, page: Int, limit: Int) =
        apiService.getProductSeller(token, id, type, page, limit)

    suspend fun getShopAccount(token: String, id: String) = apiService.getShopInfo(token, id)

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
    ) = apiService.postOneProduct(
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