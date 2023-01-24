package com.example.ptpn.ui.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.ptpn.R
import com.example.ptpn.customview.Type
import com.example.ptpn.databinding.FragmentRegisterBinding
import com.example.ptpn.utils.ToastState
import com.example.ptpn.utils.isValidEmail
import com.example.ptpn.utils.isValidPassword
import com.example.ptpn.utils.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {

    private lateinit var binding : FragmentRegisterBinding
    private val navController by lazy { findNavController() }
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        binding.bind()
    }

    private fun FragmentRegisterBinding.bind(){
        email.editText?.doOnTextChanged { text, _, _, _ ->
            if(text.toString().isValidEmail()){
                email.isErrorEnabled = false
            }else{
                email.error = getString(R.string.txt_error_email)
            }
            if (enableButton()) btnRegister.type(Type.ENABLED.value) else btnRegister.type(Type.DISABLED.value)
        }

        password.editText?.doOnTextChanged { text, _, _, _ ->
            if(text.toString().isValidPassword()){
                password.isErrorEnabled = false
            }else{
                password.error = getString(R.string.txt_error_password)
            }
            if (enableButton()) btnRegister.type(Type.ENABLED.value) else btnRegister.type(Type.DISABLED.value)
        }

        action()
    }

    private fun FragmentRegisterBinding.action(){
        btnRegister.setOnClickListener {
            if(enableButton()){
                val email = email.editText?.text.toString()
                val password = password.editText?.text.toString()
                btnRegister.type(Type.LOADING.value)
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) { registerTask ->
                    if (registerTask.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                requireContext().toast(
                                    message = getString(R.string.toast_success_send_verify_account),
                                    state = ToastState.SUCCESS,
                                    image = R.drawable.ic_email
                                )
                                navController.navigateUp()
                            }
                        }
                    } else {
                        btnRegister.type(Type.ENABLED.value)
                        requireContext().toast(
                            message = getString(R.string.toast_success_send_verify_account),
                            state = ToastState.ERROR
                        )
                    }
                }
            }
        }

        icBack.setOnClickListener {
            navController.navigateUp()
        }
    }

    private fun enableButton(): Boolean{
        return binding.email.editText?.text.toString().isValidEmail() && binding.password.editText?.text.toString().isValidPassword()
    }
}