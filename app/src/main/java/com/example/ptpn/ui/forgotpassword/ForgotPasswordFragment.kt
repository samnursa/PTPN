package com.example.ptpn.ui.forgotpassword

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.ptpn.R
import com.example.ptpn.customview.Type
import com.example.ptpn.databinding.FragmentForgotPasswordBinding
import com.example.ptpn.utils.ToastState
import com.example.ptpn.utils.isValidEmail
import com.example.ptpn.utils.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private val navController by lazy { findNavController() }
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bind()
    }

    private fun FragmentForgotPasswordBinding.bind(){
        auth = Firebase.auth

        email.editText?.doOnTextChanged { text, _, _, _ ->
            if(text.toString().isValidEmail()){
                email.isErrorEnabled = false
                btnForgotPassword.type(Type.ENABLED.value)
            }else{
                email.error = getString(R.string.txt_error_email)
            }
        }

        action()
    }

    private fun FragmentForgotPasswordBinding.action(){
        icBack.setOnClickListener {
            navController.navigateUp()
        }

        btnForgotPassword.setOnClickListener {
            if(email.editText?.text.toString().isValidEmail()){
                val email = email.editText?.text.toString()
                btnForgotPassword.type(Type.LOADING.value)
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            requireContext().toast(
                                message = getString(R.string.toast_success_send_forgot_password),
                                state = ToastState.SUCCESS,
                                image = R.drawable.ic_email
                            )
                            navController.navigateUp()
                        }
                    }
            }
        }
    }

}