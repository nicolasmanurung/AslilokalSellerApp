package com.aslilokal.mitra.model.remote.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FCMBuyerRequest(
    val notification: Notification,
    val to: String
) : Parcelable

@Parcelize
data class Notification(
    val body: String,
    val title: String
) : Parcelable