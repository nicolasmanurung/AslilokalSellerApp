package com.kodelapo.mitra.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatusResponse(
    val message: String,
    val success: Boolean
) : Parcelable