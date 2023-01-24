package com.example.ptpn.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.ptpn.R
import com.example.ptpn.customview.Type
import com.example.ptpn.databinding.FragmentLoginBinding
import com.example.ptpn.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val navController by lazy { findNavController() }
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bind()
    }

    private fun FragmentLoginBinding.bind(){
        auth = Firebase.auth
        binding.linkRegister.spannable()
        binding.linkForgotPassword.spannable()
        email.editText?.doOnTextChanged { text, _, _, _ ->
            if(text.toString().isValidEmail()){
                email.isErrorEnabled = false
            }else{
                email.error = getString(R.string.txt_error_email)
            }
            if (enableButton()) btnLogin.type(Type.ENABLED.value) else btnLogin.type(Type.DISABLED.value)
        }

        password.editText?.doOnTextChanged { text, _, _, _ ->
            if(text.toString().isValidPassword()){
                password.isErrorEnabled = false
            }else{
                password.error = getString(R.string.txt_error_password)
            }
            if (enableButton()) btnLogin.type(Type.ENABLED.value) else btnLogin.type(Type.DISABLED.value)
        }
        action()
    }

    private fun FragmentLoginBinding.action(){
        linkRegister.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        linkForgotPassword.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        btnLogin.setOnClickListener {
            if(enableButton()){
                val email = email.editText?.text.toString()
                val password = password.editText?.text.toString()
                btnLogin.type(Type.LOADING.value)
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if(user?.isEmailVerified == true){

                            navController.navigate(R.id.action_loginFragment_to_sptaFragment)
                        }else{
                            btnLogin.type(Type.ENABLED.value)
                            requireContext().toast(
                                message = getString(R.string.txt_error_account_not_verified),
                                state = ToastState.ERROR
                            )
                        }
                    } else {
                        requireContext().toast(
                            message = getString(R.string.txt_error_account_not_register),
                            state = ToastState.ERROR
                        )
                        btnLogin.type(Type.ENABLED.value)
                    }
                }
            }
        }
    }

    private fun enableButton(): Boolean{
        return binding.email.editText?.text.toString().isValidEmail() && binding.password.editText?.text.toString().isValidPassword()
    }
}