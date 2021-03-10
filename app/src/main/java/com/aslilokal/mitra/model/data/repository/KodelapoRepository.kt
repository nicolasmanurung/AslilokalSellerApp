package com.aslilokal.mitra.model.data.repository

import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.remote.request.LoginRequest
import com.aslilokal.mitra.model.remote.request.OneProduct
import com.aslilokal.mitra.model.remote.request.PesananRequest
import com.aslilokal.mitra.model.remote.request.RegisterRequest
import com.aslilokal.mitra.model.remote.response.DebtorItem
import com.aslilokal.mitra.model.remote.response.LoginResponse
import com.aslilokal.mitra.model.remote.response.VoucherItem
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

    suspend fun getOneProduct(token: String, id: String) = apiHelper.getOneProduct(token, id)

    suspend fun putOneProduct(
        token: String,
        id: String,
        dataProduct: OneProduct
    ) = apiHelper.putOneProduct(
        token,
        id,
        dataProduct
    )

    suspend fun putOneImage(
        token: String,
        imageStream: MultipartBody.Part,
        imgKey: RequestBody
    ) = apiHelper.putOneImage(token, imageStream, imgKey)

    suspend fun getOrder(
        token: String,
        idUser: String,
        status: String
    ) = apiHelper.getOrderByStatus(token, idUser, status)

    suspend fun putStatusOrder(
        token: String,
        idOrder: String,
        pesananRequest: PesananRequest
    ) = apiHelper.putStatusOrder(token, idOrder, pesananRequest)

    suspend fun getAllNotification(
        token: String,
        idSeller: String
    ) = apiHelper.getAllNotificationSeller(token, idSeller)

    suspend fun postRegister(
        registerRequest: RegisterRequest
    ) = apiHelper.postSellerRegister(registerRequest)

    suspend fun getToken(
        authToken: String,
        verifToken: String
    ) = apiHelper.getToken(authToken, verifToken)

    suspend fun postResubmitTokenVerify(
        token: String
    ) = apiHelper.postResubmitToken(token)

    suspend fun postSellerBio(
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
    ) = apiHelper.postSellerBiodata(
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

    suspend fun postShopBio(
        token: String,
        imgShop: MultipartBody.Part,
        idSellerAccount: RequestBody,
        nameShop: RequestBody,
        noTelpSeller: RequestBody,
        noWhatsappShop: RequestBody,
        isPickup: RequestBody,
        isDelivery: RequestBody,
        freeOngkirLimitKm: RequestBody,
        addressShop: RequestBody,
        postalCode: RequestBody,
        isTwentyFourHours: RequestBody,
        openTime: RequestBody,
        closeTime: RequestBody
    ) = apiHelper.postShopBiodata(
        token,
        imgShop,
        idSellerAccount,
        nameShop,
        noTelpSeller,
        noWhatsappShop,
        isPickup,
        isDelivery,
        freeOngkirLimitKm,
        addressShop,
        postalCode,
        isTwentyFourHours,
        openTime,
        closeTime
    )

    suspend fun putRequestShopRegister(
        token: String,
        idUser: String,
        status: RequestBody
    ) = apiHelper.putRequestRegistrationShop(token, idUser, status)

    suspend fun getDebtorByMonth(
        token: String,
        idUser: String,
        year: String,
        month: String
    ) = apiHelper.getDebtorByMonth(token, idUser, year, month)

    suspend fun postOneDebtor(
        token: String,
        debtor: DebtorItem
    ) = apiHelper.postOneDebtor(token, debtor)

    suspend fun putOneDebtor(
        token: String,
        idDebtor: String,
        debtor: DebtorItem
    ) = apiHelper.putOneDebtor(token, idDebtor, debtor)

    suspend fun getLatestTransaction(
        token: String,
        idUser: String,
        month: Int,
        year: Int
    ) = apiHelper.getLatestTransaction(token, idUser, month, year)

    suspend fun getTotalRevenue(
        token: String,
        idUser: String
    ) = apiHelper.getTotalRevenue(token, idUser)

    suspend fun getAllRequestRevenue(
        token: String,
        idUser: String
    ) = apiHelper.getAllListRevenueRequest(token, idUser)

    suspend fun getAllVouchers(
        token: String,
        idUser: String
    ) = apiHelper.getAllVouchers(token, idUser)

    suspend fun postOneVoucher(
        token: String,
        oneVoucher: VoucherItem
    ) = apiHelper.postOneVoucher(token, oneVoucher)
}