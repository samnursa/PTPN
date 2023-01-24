package com.example.ptpn.utils

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.TextView

fun TextView.spannable(){
    val content = SpannableString(this.text)
    content.setSpan(UnderlineSpan(), 0, content.length, 0)
    this.text = content
}