package com.aslilokal.mitra.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RevenueTotalResponse(
    val idSellerAccount: String,
    val sumSaldo: Int,
    val result: RevenueTotal? = null
) : Parcelable

@Parcelize
data class RevenueTotal(
    val __v: Int?,
    val _id: String?,
    val idSellerAccount: String,
    val sumSaldo: String
) : Parcelable
