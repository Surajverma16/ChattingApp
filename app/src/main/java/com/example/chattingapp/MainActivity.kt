package com.example.chattingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.chattingapp.authorization.SignInPage
import com.example.chattingapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var onlineStatus: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, SignInPage())
            commit()
        }
    }

    override fun onStart() {
        super.onStart()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            onlineStatus = true
            val status = HashMap<String, Any>()
            status.put("onlineStatus", onlineStatus)
            FirebaseDatabase.getInstance().getReference("User/$userId").updateChildren(status)
        }
    }

    override fun onPause() {
        super.onPause()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            onlineStatus = false
            val status = HashMap<String, Any>()
            status.put("onlineStatus", onlineStatus)
            FirebaseDatabase.getInstance().getReference("User/$userId").updateChildren(status)

            val lastSeen = HashMap<String, Any>()
            lastSeen.put(
                "lastTime",
                SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time)
            )
            FirebaseDatabase.getInstance().getReference("User/$userId").updateChildren(lastSeen)

            val lastDate = HashMap<String, Any>()
            lastDate.put(
                "lastDate",
                SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().time)
            )
            FirebaseDatabase.getInstance().getReference("User/$userId").updateChildren(lastDate)

        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(currentFragment is MainTabLayout){
            finish()
        }else{
            supportFragmentManager.popBackStack()
        }
    }
}