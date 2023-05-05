package com.example.chattingapp.authorization

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chattingapp.HomePage
import com.example.chattingapp.MainTabLayout
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentSignInPageBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class SignInPage : Fragment() {

    lateinit var binding: FragmentSignInPageBinding
    lateinit var auth : FirebaseAuth
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignInPageBinding.inflate(layoutInflater,container,false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSendOTP.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            sendOtp(binding.editNumber.text.toString())
        }
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, MainTabLayout())
                    addToBackStack(this@SignInPage.toString())
                    commit()
                }
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Log.e("Verification Failed", p0.localizedMessage!!.toString())
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                 storedVerificationId = p0
                        resendToken = p1
                binding.progressBar.visibility = View.GONE
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, OtpVerificationPage(storedVerificationId, binding.editNumber.text.toString()))
                    commit()
                }
            }
        }
    }


    fun sendOtp(number: String) {
        if (number.isNotEmpty()) {
            val numbers = "+91$number"
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(numbers)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            Toast.makeText(requireContext(), "Enter Your Number", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser?.uid != null){
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, MainTabLayout())
                commit()
            }
        }
    }



}