package com.example.chattingapp

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chattingapp.databinding.FragmentViewImageBinding
import com.example.chattingapp.model.DisplayAllUser
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class ViewImage(val displayAllUser: DisplayAllUser) : Fragment() {

    lateinit var binding: FragmentViewImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentViewImageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imgBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, MainTabLayout())
                commit()
            }
        }

        binding.txtNameTitle.text = displayAllUser.name

        val LocalFile = File.createTempFile("tempfile", "jpeg")
        FirebaseStorage.getInstance()
            .getReference("Users/Profile_Pictures/+91${displayAllUser.number}/${displayAllUser.profileImgName}")
            .getFile(LocalFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                binding.imgFullImage.setImageBitmap(bitmap)
            }
    }
}