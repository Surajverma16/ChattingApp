package com.example.chattingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.replace
import android.view.MenuItem
import com.example.chattingapp.authorization.ProfilePage
import com.example.chattingapp.authorization.SignInPage
import com.example.chattingapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, SignInPage())
            commit()
        }


    }

}