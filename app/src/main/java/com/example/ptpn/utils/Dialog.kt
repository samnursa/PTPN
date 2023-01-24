package com.example.ptpn.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.ptpn.databinding.CustomDialogBinding

interface DialogUtils {
    fun Context.regularDialog(
        image: Int? = null,
        title: String? = null,
        desc: String? = null,
        buttonNo: Pair<String, (Context) -> Unit>? = null,
        buttonYes: Pair<String, (Context) -> Unit>? = null
    ){
        val binding = CustomDialogBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.setLayout(600, 400)
        image?.let {
            binding.imgIcon.visible()
            Glide.with(this).load(it).into(binding.imgIcon)
        }
        title?.let {
            binding.tvTitle.apply {
                visible()
                text = it
            }
        }
        desc?.let {
            binding.tvDesc.apply {
                visible()
                text = it
            }
        }
        buttonNo?.let { pair ->
            binding.btnNo.apply {
                visible()
                text = pair.first
                setOnClickListener {
                    pair.second(this@regularDialog)
                    dialog.dismiss()
                }
            }
        }
        buttonYes?.let { pair ->
            binding.btnYes.apply {
                visible()
                text(pair.first)
                setOnClickListener {
                    pair.second(this@regularDialog)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.80).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}