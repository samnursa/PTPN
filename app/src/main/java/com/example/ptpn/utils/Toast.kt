package com.example.ptpn.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.ptpn.R
import com.example.ptpn.databinding.CustomToastBinding


fun Context.toast(message: CharSequence, state: ToastState = ToastState.SUCCESS, image: Int? = null) {
    val binding = CustomToastBinding.inflate(LayoutInflater.from(this))
    when(state){
        ToastState.SUCCESS -> {
            binding.cardToast.setBackgroundResource(R.drawable.toast_success)
        }
        ToastState.ERROR -> {
            binding.cardToast.setBackgroundResource(R.drawable.toast_failed)
        }
    }

    image?.let{
        binding.imgIcon.also { img ->
            img.visible()
            Glide.with(this).load(it).into(img)
        }
    }

    binding.txtMessage.text = message
    val toast = Toast(this)
    toast.duration = Toast.LENGTH_SHORT
    toast.setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, 0)
    toast.view = binding.root
    toast.show()
}

enum class ToastState(val toastState: String) {
    SUCCESS("success"),
    ERROR("error")
}