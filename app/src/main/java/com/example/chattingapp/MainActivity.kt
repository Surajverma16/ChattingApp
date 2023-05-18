package com.example.chattingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.chattingapp.authorization.SignInPage
import com.example.chattingapp.databinding.ActivityMainBinding
import com.example.chattingapp.model.DisplayUserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var onlineStatus: Boolean = false
    private var userData = DisplayUserData()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, SignInPage())
            commit()
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().getReference("User/$userId").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val listingData = snapshot.getValue(DisplayUserData::class.java)
                    listingData?.key = snapshot.key.toString()
                    userData = listingData!!
                    addCallFragment()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
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


    private fun addCallFragment() {


        val application = application
        val appID: Long = 461464117
        val appSign: String = "7e7809db75558a171a9933906fd32659591619964dccee720bde932ca4e93e7c"
        val userID: String = userData.key
        val userName: String = userData.name

        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()/*
        callInvitationConfig.notifyWhenAppRunningInBackgroundOrQuit = true
        val notificationConfig = ZegoNotificationConfig()
        notificationConfig.sound = "zego_uikit_sound_call"
        notificationConfig.channelID = "CallInvitation"
        notificationConfig.channelName = "CallInvitation"*/
        ZegoUIKitPrebuiltCallInvitationService.init(
            application,
            appID,
            appSign,
            userID,
            userName,
            callInvitationConfig
        )
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is MainTabLayout) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ZegoUIKitPrebuiltCallInvitationService.unInit()
    }
}