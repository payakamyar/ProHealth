package com.projekt.prohealth.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.FragmentLoginBinding
import com.projekt.prohealth.ui.activity.MainActivity
import com.projekt.prohealth.utility.InputValidation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    @Inject lateinit var auth:FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        setup()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setFragmentResultListener("register"){_,result ->
            if(result.getBoolean("isRegistered"))
                showAlertDialog()
        }
    }

    private fun setup(){
        binding.apply {
            registerText.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            forgotpassTv.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
            }
            loginBtn.setOnClickListener {
                if(validateInput()){
                    (it as Button).text = "Logging in..."
                    it.isEnabled = false
                    handleLogin()}
            }
        }
    }

    private fun handleLogin(){
        auth.signInWithEmailAndPassword(binding.emailField.text.toString(),binding.passwordField.text.toString()).addOnCompleteListener {
            if(it.isSuccessful){
                val i = Intent(requireContext(),MainActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
                requireActivity().finish()
            }else{
                binding.errorTextview.text = it.exception!!.message
            }
            binding.loginBtn.text = "Login"
            binding.loginBtn.isEnabled = true
        }
    }

    private fun validateInput():Boolean{
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
        return true
    }

    private fun showAlertDialog(){
        val view = layoutInflater.inflate(R.layout.registered_dialog,binding.root)
        val dialog = AlertDialog.Builder(requireContext()).setView(view).create().apply {
            view.findViewById<Button>(R.id.ok_btn).setOnClickListener { this.dismiss() }
        }
        dialog.show()
    }

}