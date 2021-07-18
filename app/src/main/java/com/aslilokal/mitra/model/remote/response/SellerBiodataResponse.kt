package com.aslilokal.mitra.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SellerBiodataResponse(
    val message: String,
    val result: Seller,
    val success: Boolean
) : Parcelable

@Parcelize
data class Seller(
    val __v: Int,
    val _id: String,
    val addressSeller: String,
    val birthDateSeller: String?,
    val idKtpNumber: String,
    val idSellerAccount: String,
    val imgSelfSeller: String,
    val ktpImgSeller: String,
    val nameSellerBiodata: String,
    val paymentInfo: PaymentInfo,
    val telpNumber: String
) : Parcelable

@Parcelize
data class PaymentInfo(
    val danaNumber: String?,
    val gopayNumber: String?,
    val ovoNumber: String?
) : Parcelable
