package com.example.chattingapp.authorization

import android.os.Bundle
import android.text.TextUtils.replace
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chattingapp.HomePage
import com.example.chattingapp.MainActivity
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentOtpVerificationPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class OtpVerificationPage(val storedVerificationId: String, val number: String) : Fragment() {

    lateinit var binding: FragmentOtpVerificationPageBinding
    lateinit var auth: FirebaseAuth
    lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOtpVerificationPageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        binding.btnVerifyOTP.setOnClickListener {
            if (binding.editOtpVerification.text.toString().isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId, binding.editOtpVerification.text.toString()
                )
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) {
            if (it.isSuccessful) {

                val fragment = ProfilePage()
                val arg = Bundle()
                arg.putString("Number", number)
                fragment.arguments = arg
                binding.progressBar.visibility = View.GONE
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, fragment)
                    commit()
                }
            } else {
                Toast.makeText(requireContext(), it.exception?.message, Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }
}