package com.example.chattingapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide.init
import com.example.chattingapp.adapter.StatusAdapter
import com.example.chattingapp.databinding.FragmentStatusBinding
import com.example.chattingapp.model.DisplayAllUser
import com.example.chattingapp.model.DisplayUserData
import com.example.chattingapp.model.StatusModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Status : Fragment() {

    lateinit var binding: FragmentStatusBinding
    lateinit var storage: StorageReference
    private var addedUser = arrayListOf<DisplayAllUser>()
    private var UserStatus = arrayListOf<StatusModel>()
    private var userData: DisplayUserData? = null
    lateinit var statusAdapter: StatusAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    init {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().getReference("User/$userId/personalChat")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            addedUser = arrayListOf()
                            for (i in snapshot.children) {
                                val listData = i.getValue(DisplayAllUser::class.java)
                                listData?.key = i.key.toString()
                                addedUser.add(listData!!)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                }
            )
        UserStatus = arrayListOf()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(requireContext(), "Status", Toast.LENGTH_SHORT).show()
        storage = Firebase.storage.reference
        val userNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val LocalFile = File.createTempFile("tempfile", "jpeg")
        storage.child("Users/Profile_Pictures/$userNumber/$userId")
            .getFile(LocalFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                binding.imgUserImage.setImageBitmap(bitmap)
            }

        binding.btnAddStatus.setOnClickListener {
            Toast.makeText(requireContext(), "Camera", Toast.LENGTH_SHORT).show()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1)
        }
        binding.myStatusLayout.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1)
        }


        for (i in addedUser) {
            FirebaseDatabase.getInstance().getReference("Status/${i.userID}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (item in snapshot.children) {
                                val statusData = item.getValue(StatusModel::class.java)
                                UserStatus.add(statusData!!)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Status Database :", error.message)
                    }
                })
        }

        statusAdapter = StatusAdapter(UserStatus, object : StatusAdapter.onClickedStatus {
            override fun openStatus(statusModel: StatusModel) {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, ViewStatus(statusModel))
                    addToBackStack(this@Status.toString())
                    commit()
                }
            }
        })

        binding.recyclerViewStatus.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewStatus.adapter = statusAdapter

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Log.d("Image data", data.toString())
            val photo: Bitmap = data!!.extras!!["data"] as Bitmap
            Log.d("photo", photo.toString())
            val baos = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            val pushId = FirebaseDatabase.getInstance().getReference("User").push().key
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            FirebaseStorage.getInstance().getReference("Users/Status/$userId/$pushId")
                .putBytes(byteArray)
                .addOnSuccessListener {

                    val statusData = StatusModel(
                        pushId!!,
                        SimpleDateFormat("MM/dd/yyyy hh:mm a").format(Calendar.getInstance().time),
                        userData!!.name,
                        userId!!,
                        FirebaseAuth.getInstance().currentUser?.phoneNumber!!,
                    )
                    FirebaseDatabase.getInstance().getReference("Status/$userId")
                        .child(pushId).setValue(statusData).addOnSuccessListener {
                            Toast.makeText(requireContext(), "Status Uploaded", Toast.LENGTH_SHORT)
                                .show()

                        }
                }
        }
    }
}