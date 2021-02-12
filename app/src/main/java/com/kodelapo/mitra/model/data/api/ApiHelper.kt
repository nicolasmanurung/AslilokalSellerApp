package com.kodelapo.mitra.model.data.api

import com.kodelapo.mitra.model.remote.request.LoginRequest
import com.kodelapo.mitra.model.remote.request.OneProduct
import com.kodelapo.mitra.model.remote.request.PesananRequest
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

    suspend fun getOneProduct(token: String, id: String) = apiService.getOneProduct(token, id)

    suspend fun putOneProduct(
        token: String,
        id: String,
        dataProduct: OneProduct
    ) = apiService.putOneProduct(
        token,
        id,
        dataProduct
    )

    suspend fun putOneImage(
        token: String,
        imagePart: MultipartBody.Part,
        imgKey: RequestBody
    ) = apiService.putImageProduct(token, imgKey, imagePart)

    suspend fun getOrderByStatus(
        token: String,
        idUser: String,
        status: String
    ) = apiService.getOrderByStatus(token, idUser, status)

    suspend fun putStatusOrder(
        token: String,
        idOrder: String,
        pesananRequest: PesananRequest
    ) = apiService.putStatusOneOrder(token, idOrder, pesananRequest)
}