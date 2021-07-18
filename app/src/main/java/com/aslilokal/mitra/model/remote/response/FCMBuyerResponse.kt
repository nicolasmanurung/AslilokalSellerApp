package com.aslilokal.mitra.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FCMBuyerResponse(
    val message_id: Long
) : Parcelable