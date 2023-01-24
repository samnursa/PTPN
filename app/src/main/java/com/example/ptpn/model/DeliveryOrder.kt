package com.example.ptpn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeliveryOrder(
    val ha_tertebang: Int?,
    val tgl_tebang: String?,
    val no_lori: String?,
    val no_truk: String?,
    val sopir: String?,
    val brix: Int?,
    val ph: Int?,
    val terbakar: Boolean?
) : Parcelable, Model() {
    constructor(): this(0, "", "", "", "", 0, 0, false)
}