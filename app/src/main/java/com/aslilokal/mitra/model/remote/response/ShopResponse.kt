package com.aslilokal.mitra.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopResponse(
    val message: String,
    val result: Shop,
    val success: Boolean
) : Parcelable

@Parcelize
data class Shop(
    val __v: Int?,
    val _id: String?,
    val addressShop: String,
    val closeTime: String,
    val idSellerAccount: String,
    val imgShop: String,
    val isDelivery: Boolean,
    val isShopFreeDelivery: Boolean,
    val isPickup: Boolean,
    val isTwentyFourHours: Boolean,
    val postalCodeInput: String?,
    val nameShop: String,
    val noTelpSeller: String,
    val noWhatsappShop: String,
    val openTime: String,
    val sumFollowers: Int?,
    val sumCountView: Int?,
    val shopTypeStatus: String?,
    val rajaOngkir: RajaOngkirAddress?
) : Parcelable

@Parcelize
data class RajaOngkirAddress(
    val city_id: String,
    val city_name: String,
    val postal_code: String,
    val province: String,
    val province_id: String,
    val type: String?
) : Parcelable