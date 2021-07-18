package com.aslilokal.mitra.model.data.api

import com.aslilokal.mitra.model.remote.request.LoginRequest
import com.aslilokal.mitra.model.remote.request.OneProduct
import com.aslilokal.mitra.model.remote.request.PesananRequest
import com.aslilokal.mitra.model.remote.request.RegisterRequest
import com.aslilokal.mitra.model.remote.response.DebtorItem
import com.aslilokal.mitra.model.remote.response.Shop
import com.aslilokal.mitra.model.remote.response.VoucherItem
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ApiHelper(private val apiService: AslilokalAPI) {
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

    suspend fun getDetailOrder(
        key: String,
        orderId: String
    ) = apiService.getDetailOrder(key, orderId)

    suspend fun putStatusOrder(
        token: String,
        idOrder: String,
        pesananRequest: PesananRequest
    ) = apiService.putStatusOneOrder(token, idOrder, pesananRequest)

    suspend fun getAllNotificationSeller(
        token: String,
        idSeller: String
    ) = apiService.getAllNotificationSeller(token, idSeller)

    suspend fun postSellerRegister(
        registerRequest: RegisterRequest
    ) = apiService.postSellerRegister(registerRequest)

    suspend fun getToken(
        authToken: String,
        verifToken: String
    ) = apiService.getToken(authToken, verifToken)

    suspend fun postResubmitToken(
        token: String
    ) = apiService.postResubmitToken(token)

    suspend fun postSellerBiodata(
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
    ) = apiService.postSellerAccount(
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

    suspend fun postShopBiodata(
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
    ) = apiService.postShopInfo(
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
        cityId,
        provinceId,
        province, cityName, postalCodeRO
    )

    suspend fun putRequestRegistrationShop(
        token: String,
        idUser: String,
        status: String
    ) = apiService.putRegistrationSubmit(token, idUser, status)

    suspend fun getDebtorByMonth(
        token: String,
        idUser: String,
        year: String,
        month: String
    ) = apiService.getDebtorByMonth(token, idUser, year, month)

    suspend fun postOneDebtor(
        token: String,
        debtor: DebtorItem
    ) = apiService.postOneDebtor(token, debtor)

    suspend fun putOneDebtor(
        token: String,
        idDebtor: String,
        debtor: DebtorItem
    ) = apiService.putOneDebtor(token, idDebtor, debtor)

    suspend fun getLatestTransaction(
        token: String,
        idUser: String,
        month: Int,
        year: Int
    ) = apiService.getLatestTransaction(token, idUser, month, year)

    suspend fun getTotalRevenue(
        token: String,
        idUser: String
    ) = apiService.getTotalRevenue(token, idUser)

    suspend fun getAllListRevenueRequest(
        token: String,
        idUser: String
    ) = apiService.getAllListRevenueRequest(token, idUser)

    suspend fun getAllVouchers(
        token: String,
        idUser: String
    ) = apiService.getAllVoucher(token, idUser)

    suspend fun postOneVoucher(
        token: String,
        oneVoucher: VoucherItem
    ) = apiService.postOneVoucher(token, oneVoucher)

    suspend fun getCitiesRO(
        key: String
    ) = apiService.ROGetCities(key)

    suspend fun putShopInfo(
        token: String,
        username: String,
        shopData: Shop
    ) = apiService.putShopInfo(token, username, shopData)
}