package com.kodelapo.mitra.model.remote.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginRequest(
    var emailSeller: String,
    var passSeller: String
) : Parcelable