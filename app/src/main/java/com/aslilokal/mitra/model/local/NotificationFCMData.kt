package com.aslilokal.mitra.model.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationFCMData(
    val title: String,
    val data: String
) : Parcelable