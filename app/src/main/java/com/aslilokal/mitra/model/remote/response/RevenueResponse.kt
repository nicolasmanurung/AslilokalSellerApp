package com.aslilokal.mitra.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RevenueResponse(
    val message: String,
    val result: ArrayList<RevenueItem>,
    val success: Boolean
) : Parcelable

@Parcelize
data class RevenueItem(
    val __v: Int,
    val _id: String,
    val acceptedRevenue: Int,
    val createdAt: String,
    val idSellerAccount: String,
    val informationPayment: InformationPayment,
    val statusRevenue: String,
    val sumRevenueRequest: Int,
    val acceptAt: String
) : Parcelable

@Parcelize
data class InformationPayment(
    val numberPayment: Long,
    val providerPayment: String
) : Parcelable