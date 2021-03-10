package com.aslilokal.mitra.model.remote.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RegisterRequest(
    val emailSeller: String,
    val passSeller: String,
    val shopVerifyStatus: String
) : Parcelable