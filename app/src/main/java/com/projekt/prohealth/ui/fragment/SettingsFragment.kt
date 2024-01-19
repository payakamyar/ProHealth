package com.projekt.prohealth.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.projekt.prohealth.R
import com.projekt.prohealth.databinding.FragmentSettingsBinding
import com.projekt.prohealth.ui.activity.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding:FragmentSettingsBinding
    @Inject lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        setup()
        return binding.root
    }

    private fun setup(){
        binding.apply {
            if(auth.currentUser == null){
                loginBtn.visibility = View.VISIBLE
                logoutBtn.visibility = View.GONE
                nameTv.text = ""
            }else{
                loginBtn.visibility = View.GONE
                logoutBtn.visibility = View.VISIBLE
                nameTv.text = "Hi ${auth.currentUser!!.displayName}"
            }
            logoutBtn.setOnClickListener {
                auth.signOut()
                startActivity(Intent(requireContext(),LoginActivity::class.java))
                requireActivity().finish()
            }
            loginBtn.setOnClickListener {
                startActivity(Intent(requireContext(),LoginActivity::class.java))
            }
        }
    }

}