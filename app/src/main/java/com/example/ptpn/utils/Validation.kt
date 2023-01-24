package com.example.ptpn.utils

import java.util.regex.Pattern

fun String.isValidPassword(): Boolean{
    val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{6,}$")
    return passwordPattern.matcher(this).matches()
}

fun String.isValidEmail(): Boolean{
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}