package com.projekt.prohealth.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.FragmentRegisterBinding
import com.projekt.prohealth.ui.activity.MainActivity
import com.projekt.prohealth.utility.InputValidation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    @Inject lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        setup()
        return binding.root
    }


    private fun setup(){
        binding.apply {
            registerBtn.setOnClickListener {
                handleRegistration()
            }
        }
    }


    private fun handleRegistration(){
        if(validateInputs()){
            auth.createUserWithEmailAndPassword(binding.emailField.text.toString(),binding.passwordField.text.toString()).addOnCompleteListener {
                if(it.isSuccessful){
                    val changeRequest = UserProfileChangeRequest.Builder()
                    changeRequest.apply {
                        displayName = binding.nameField.text.toString()
                        build()
                        findNavController().popBackStack()
                    }
                }
                else{
                    binding.errorTextview.text = it.exception!!.message
                }
            }
        }
    }

    private fun validateInputs():Boolean{
        val emailResult = InputValidation.validateEmail(binding.emailField.text.toString())
        val errorTv = binding.errorTextview
        if(!emailResult.first){
            errorTv.text = emailResult.second
            return false
        }
        val passwordResult = InputValidation.validatePassword(binding.passwordField.text.toString())
        if(!passwordResult.first){
            errorTv.text = passwordResult.second
            return false
        }
        if(binding.emailField.text.toString() != binding.repeatEmailField.text.toString()){
            errorTv.text = "The emails you provided in both fields are not identical."
            return false
        }
        if(binding.passwordField.text.toString() != binding.repeatPasswordField.text.toString()){
            errorTv.text = "The passwords you provided in both fields are not identical."
            return false
        }
        return true
    }

}