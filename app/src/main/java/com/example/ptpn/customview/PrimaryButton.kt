package com.example.ptpn.customview

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.ptpn.R
import com.example.ptpn.databinding.CustomPrimaryButtonBinding
import com.example.ptpn.utils.disable
import com.example.ptpn.utils.enable
import com.example.ptpn.utils.gone
import com.example.ptpn.utils.visible

class PrimaryButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr) {

    var option = Type.ENABLED.value
    var binding : CustomPrimaryButtonBinding

    init {
        binding = CustomPrimaryButtonBinding.inflate(LayoutInflater.from(context), this, true)
        attrs?.let {
            val styledAttributes = context.obtainStyledAttributes(it, R.styleable.Button, 0, 0)
            val text = styledAttributes.getString(R.styleable.Button_text)
            option = styledAttributes.getInt(R.styleable.Button_type, Type.ENABLED.value)
            text(text)
            type(option)
        }
    }

    fun text(text: String?) {
        if (text != null) {
            binding.btnText.text = text
        }
    }

    fun type(option: Int?) {
        when (option) {
            Type.DISABLED.value -> {
                binding.btnPrimary.background = ContextCompat.getDrawable(context, R.drawable.btn_disable)
                if (Build.VERSION.SDK_INT < 23) {
                    binding.btnText.setTextAppearance(context, R.style.open_sans_bold_14sp_light_gray)
                } else {
                    binding.btnText.setTextAppearance(R.style.open_sans_bold_14sp_light_gray)
                }
                binding.btnText.visible()
                binding.pbButton.gone()
                binding.btnPrimary.disable()
            }
            Type.LOADING.value -> {
                binding.btnPrimary.background = ContextCompat.getDrawable(context, R.drawable.btn_disable)
                binding.btnText.gone()
                binding.pbButton.visible()
                binding.btnPrimary.disable()
            }
            else -> {
                binding.btnPrimary.background = ContextCompat.getDrawable(context, R.drawable.btn_primary)
                if (Build.VERSION.SDK_INT < 23) {
                    binding.btnText.setTextAppearance(context, R.style.open_sans_bold_14sp_white)
                } else {
                    binding.btnText.setTextAppearance(R.style.open_sans_bold_14sp_white)
                }
                binding.btnText.visible()
                binding.pbButton.gone()
                binding.btnPrimary.enable()
            }
        }
    }
}

enum class Type(val value: Int) {
    ENABLED(0),
    DISABLED(1),
    LOADING(2)
}