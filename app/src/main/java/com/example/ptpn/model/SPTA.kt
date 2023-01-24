package com.example.ptpn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SPTA(
    val tanggal: String?,
    val no_petak: String?,
    val afdeling: String?,
    val kategori: String?,
    val deskripsi: String?,
    val pta: String?,
    val expired: String?
) : Parcelable, Model() {
    constructor(): this("", "", "", "", "", "", "")
}