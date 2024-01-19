package com.projekt.prohealth.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.FragmentForgotPasswordBinding
import javax.inject.Inject


class ForgotPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    @Inject lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(layoutInflater)
        setup()
        return binding.root
    }

    private fun setup(){
        binding.apply {
            changeBtn.setOnClickListener {
                auth.sendPasswordResetEmail(binding.emailField.text.toString())
                frame1.visibility = View.GONE
                frame2.visibility = View.VISIBLE
            }
            okBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

}