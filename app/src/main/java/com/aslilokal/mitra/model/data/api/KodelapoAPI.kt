package com.aslilokal.mitra.model.data.api

import com.aslilokal.mitra.model.remote.request.*
import com.aslilokal.mitra.model.remote.response.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    @Headers("Content-Type:application/json")
    @GET("seller/orders/{idUser}")
    suspend fun getOrderByStatus(
        @Header("Authorization") token: String,
        @Path("idUser") idUSer: String,
        @Query("status") status: String
    ): Response<OrderResponse>

    @Headers("Content-Type:application/json")
    @PUT("seller/order/status/{idOrder}")
    suspend fun putStatusOneOrder(
        @Header("Authorization") token: String,
        @Path("idOrder") idOrder: String,
        @Body pesananRequest: PesananRequest
    ): Response<StatusResponse>

    @Headers("Content-Type:application/json")
    @GET("seller/notifications/{idSeller}")
    suspend fun getAllNotificationSeller(
        @Header("Authorization") token: String,
        @Path("idSeller") idSeller: String
    ): Response<NotificationResponse>

    @Headers("Content-Type:application/json")
    @POST("seller/register")
    suspend fun postSellerRegister(
        @Body registerRequest: RegisterRequest
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("seller/verify/token")
    suspend fun getToken(
        @Header("Authorization") token: String,
        @Field("tokenVerify") tokenVerify: String
    ): Response<StatusResponse>

    @Headers("Content-Type:application/json")
    @POST("seller/verify")
    suspend fun postResubmitToken(
        @Header("Authorization") token: String
    ): Response<StatusResponse>

    @Multipart
    @POST("seller/account")
    suspend fun postSellerAccount(
        @Header("Authorization") token: String,
        @Part imgSelfSeller: MultipartBody.Part,
        @Part ktpImgSeller: MultipartBody.Part,
        @Part("idSellerAccount") idSellerAccount: RequestBody,
        @Part("nameSellerBiodata") nameSellerBiodata: RequestBody,
        @Part("idKtpNumber") idKtpNumber: RequestBody,
        @Part("telpNumber") telpNumber: RequestBody,
        @Part("addressSeller") addressSeller: RequestBody,
        @Part("birthDateSeller") birthDateSeller: RequestBody,
        @Part("ovoNumber") ovoNumber: RequestBody? = "".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("danaNumber") danaNumber: RequestBody? = "".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("gopayNumber") gopayNumber: RequestBody? = "".toRequestBody("text/plain".toMediaTypeOrNull())
    ): Response<StatusResponse>

    @Multipart
    @POST("seller/shop")
    suspend fun postShopInfo(
        @Header("Authorization") token: String,
        @Part imgShop: MultipartBody.Part,
        @Part("idSellerAccount") idSellerAccount: RequestBody,
        @Part("nameShop") nameShop: RequestBody,
        @Part("noTelpSeller") noTelpSeller: RequestBody,
        @Part("noWhatsappShop") noWhatsappShop: RequestBody,
        @Part("isPickup") isPickup: RequestBody,
        @Part("isDelivery") isDelivery: RequestBody,
        @Part("freeOngkirLimitKm") freeOngkirLimitKm: RequestBody,
        @Part("addressShop") addressShop: RequestBody,
        @Part("postalCode") postalCode: RequestBody,
        @Part("isTwentyFourHours") isTwentyFourHours: RequestBody,
        @Part("openTime") openTime: RequestBody? = "".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("closeTime") closeTime: RequestBody? = "".toRequestBody("text/plain".toMediaTypeOrNull())
    ): Response<StatusResponse>

    @PUT("seller/shop/request/{idUser}")
    suspend fun putRegistrationSubmit(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String,
        @Body status: RequestBody
    ): Response<StatusResponse>

    @GET("seller/debtors/{idUser}")
    suspend fun getDebtorByMonth(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String,
        @Query("year") year: String,
        @Query("month") month: String
    ): Response<DebtorResponse>

    @POST("seller/debtor")
    suspend fun postOneDebtor(
        @Header("Authorization") token: String,
        @Body debtor: DebtorItem
    ): Response<StatusResponse>

    @PUT("seller/debtor/detail/{idDebtor}")
    suspend fun putOneDebtor(
        @Header("Authorization") token: String,
        @Path("idDebtor") idDebtor: String,
        @Body debtor: DebtorItem
    ): Response<StatusResponse>

    @GET("seller/analitik/pemasukan/{idUser}")
    suspend fun getLatestTransaction(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<OrderResponse>

    @POST("seller/analitik/revenue/order")
    suspend fun postRevenueRequest(
        @Header("Authorization") token: String,
        @Body revenueRequest: RevenueItem
    ): Response<StatusResponse>

    @GET("seller/analitik/revenue/{idUser}")
    suspend fun getAllListRevenueRequest(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String
    ): Response<RevenueResponse>

    @GET("seller/analitik/revenue/total/{idUser}")
    suspend fun getTotalRevenue(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String
    ): Response<RevenueTotalResponse>

    @GET("seller/vouchers/{idUser}")
    suspend fun getAllVoucher(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String
    ): Response<VoucherResponse>

    @POST("seller/voucher")
    suspend fun postOneVoucher(
        @Header("Authorization") token: String,
        @Body oneVoucher: VoucherItem
    ) : Response<StatusResponse>
}