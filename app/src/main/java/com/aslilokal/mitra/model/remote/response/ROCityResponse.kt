package com.aslilokal.mitra.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ROCityResponse(
    val rajaongkir: Rajaongkir
) : Parcelable

@Parcelize
data class Rajaongkir(
    val query: List<String>,
    val results: ArrayList<City>,
    val status: Status
) : Parcelable

@Parcelize
data class City(
    val city_id: String,
    val city_name: String,
    val postal_code: String,
    val province: String,
    val province_id: String,
    val type: String
) : Parcelable

@Parcelize
data class Status(
    val code: Int,
    val description: String
) : Parcelable