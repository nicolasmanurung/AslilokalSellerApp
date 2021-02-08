package com.kodelapo.mitra.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginResponse(
    val success: Boolean,
    val username: String?,
    val message: String? = null,
    val token: String? = null
) : Parcelable