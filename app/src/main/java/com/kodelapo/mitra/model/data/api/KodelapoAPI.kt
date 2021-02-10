package com.kodelapo.mitra.model.data.api

import com.kodelapo.mitra.model.remote.request.LoginRequest
import com.kodelapo.mitra.model.remote.request.OneProduct
import com.kodelapo.mitra.model.remote.request.OneProductRequest
import com.kodelapo.mitra.model.remote.request.ProductRequest
import com.kodelapo.mitra.model.remote.response.LoginResponse
import com.kodelapo.mitra.model.remote.response.ProductResponse
import com.kodelapo.mitra.model.remote.response.ShopResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface KodelapoAPI {

    @Headers("Content-Type:application/json")
    @POST("seller/login")
    suspend fun postLoginSeller(@Body sellerData: LoginRequest): Response<LoginResponse>

    @Headers("Content-Type:application/json")
    @GET("seller/product")
    suspend fun getProductSeller(
        @Header("Authorization") token: String,
        @Query("idSellerAccount") idSellerAccount: String,
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ProductResponse>

    @Headers("Content-Type:application/json")
    @GET("seller/shop/detail/{idSeller}")
    suspend fun getShopInfo(
        @Header("Authorization") token: String,
        @Path("idSeller") idSeller: String
    ): Response<ShopResponse>

    @Multipart
    @POST("seller/product/")
    suspend fun postOneProduct(
        @Header("Authorization") token: String,
        @Part imgProduct: MultipartBody.Part,
        @Part("idSellerAccount") idSellerAccount: RequestBody,
        @Part("nameProduct") nameProduct: RequestBody,
        @Part("productCategory") productCategory: RequestBody,
        @Part("priceProduct") priceProduct: RequestBody,
        @Part("productWeight") productWeight: RequestBody,
        @Part("descProduct") descProduct: RequestBody,
        @Part("isAvailable") isAvailable: RequestBody,
        @Part("promoPrice") promoPrice: RequestBody
    ): Response<ProductRequest>

    @Headers("Content-Type:application/json")
    @GET("seller/product/detail/{idProduct}")
    suspend fun getOneProduct(
        @Header("Authorization") token: String,
        @Path("idProduct") idProduct: String
    ): Response<OneProductRequest>

    @Headers("Content-Type:application/json")
    @PUT("seller/product/detail/{idProduct}")
    suspend fun putOneProduct(
        @Header("Authorization") token: String,
        @Path("idProduct") idProduct: String,
        @Body dataProduct: OneProduct
    ): Response<OneProductRequest>

    @Multipart
    @PUT("seller/update/imgproduct")
    suspend fun putImageProduct(
        @Header("Authorization") token: String,
        @Part("imgKey") imgKey: RequestBody,
        @Part imgProduct: MultipartBody.Part
    ): Response<OneProductRequest>

}