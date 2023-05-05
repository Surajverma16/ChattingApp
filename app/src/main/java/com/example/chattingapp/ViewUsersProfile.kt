package com.example.chattingapp

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chattingapp.databinding.FragmentViewUsersProfileBinding
import com.example.chattingapp.model.DisplayAllUser
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class ViewUsersProfile(val displayAllUser: DisplayAllUser) : Fragment() {

    lateinit var binding: FragmentViewUsersProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentViewUsersProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtViewProfileName.text = displayAllUser.name
        binding.txtViewProfileNumber.text = displayAllUser.number

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, MainTabLayout())
                commit()
            }
        }
        val LocalFile = File.createTempFile("tempfile", "jpeg")
        FirebaseStorage.getInstance().getReference()
            .child("Users/Profile_Pictures/+91${displayAllUser.number}/${displayAllUser.userID}")
            .getFile(LocalFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                binding.imgViewProfileImage.setImageBitmap(bitmap)
            }

        binding.imgViewProfileMessage.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, Chatting(displayAllUser))
                addToBackStack(this@ViewUsersProfile.toString())
                commit()
            }
        }
    }

}