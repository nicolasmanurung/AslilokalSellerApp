package com.aslilokal.mitra.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DebtorResponse(
    val message: String,
    val result: ArrayList<DebtorItem>,
    val success: Boolean
) : Parcelable

@Parcelize
data class DebtorItem(
    val __v: Int?,
    val _id: String?,
    val createAt: String?,
    val descDebt: String,
    val idSellerAccount: String,
    val nameDebtor: String,
    val statusTransaction: Boolean,
    val totalDebt: Int
) : Parcelable