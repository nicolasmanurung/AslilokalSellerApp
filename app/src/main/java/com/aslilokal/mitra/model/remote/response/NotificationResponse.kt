package com.aslilokal.mitra.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationResponse(
    val message: String,
    val result: ArrayList<Notification>,
    val success: Boolean
) : Parcelable

@Parcelize
data class Notification(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val descNotification: String,
    val idUser: String,
    val isRead: String,
    val refId: String,
    val statusNotification: String
) : Parcelable