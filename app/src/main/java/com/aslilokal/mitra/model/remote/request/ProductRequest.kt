package com.aslilokal.mitra.model.remote.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductRequest(
    val message: String,
    val success: Boolean
) : Parcelable