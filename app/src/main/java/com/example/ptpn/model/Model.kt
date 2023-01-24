package com.example.ptpn.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
open class Model {
    @Exclude
    var id: String? = null
    fun <T : Model?> withId(id: String): T {
        this.id = id
        return this as T
    }
}