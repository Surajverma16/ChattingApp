package com.example.chattingapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import com.example.chattingapp.adapter.HomeViewPager
import com.example.chattingapp.authorization.SignInPage
import com.example.chattingapp.databinding.FragmentMainTabLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class MainTabLayout : Fragment() {

    lateinit var binding: FragmentMainTabLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainTabLayoutBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = HomeViewPager(childFragmentManager)
        adapter.addFragment(HomePage(), "Chats")
        adapter.addFragment(Status(),"Status")
        adapter.addFragment(CallHistory(), "Calls")

        binding.mainViewPager.adapter = adapter
        binding.mainTabLayout.setupWithViewPager(binding.mainViewPager)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.account -> {
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragment_container, ProfileSetting())
                        addToBackStack(this@MainTabLayout.toString())
                        commit()
                    }
                    true
                }
                R.id.signOut -> {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val status = HashMap<String, Any>()
                    status.put("onlineStatus", false)
                    FirebaseDatabase.getInstance().getReference("User/$userId").updateChildren(status)

                    val lastSeen = HashMap<String, Any>()
                    lastSeen.put(
                        "lastTime",
                        SimpleDateFormat("hh:mm a").format(Calendar.getInstance().time)
                    )
                    FirebaseDatabase.getInstance().getReference("User/$userId").updateChildren(lastSeen)

                    val lastDate = HashMap<String, Any>()
                    lastDate.put("lastDate",SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().time))
                    FirebaseDatabase.getInstance().getReference("User/$userId").updateChildren(lastDate)

                    FirebaseAuth.getInstance().signOut()
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragment_container, SignInPage())
                        commit()
                    }
                    true
                }
                else -> false
            }
        }
    }

}