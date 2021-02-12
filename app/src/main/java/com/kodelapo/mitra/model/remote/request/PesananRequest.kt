package com.kodelapo.mitra.model.remote.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PesananRequest(
    val idBuyerAccount: String,
    val statusOrder: String
) : Parcelable
