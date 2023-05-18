package com.example.chattingapp.authorization

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.chattingapp.HomePage
import com.example.chattingapp.MainTabLayout
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentProfilePageBinding
import com.example.chattingapp.model.DisplayAllUser
import com.example.chattingapp.model.SendAllUserData
import com.example.chattingapp.model.SendUserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class ProfilePage : Fragment() {

    private var number: String? = ""
    lateinit var binding: FragmentProfilePageBinding
    lateinit var database: DatabaseReference
    private var imageUri: Uri? = null
    lateinit var storage: StorageReference
    private var key: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfilePageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        number = arguments?.getString("Number")
        database = Firebase.database.reference
        storage = Firebase.storage.reference

        /* val userID = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().getReference("Users/$userID")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                        for (i in snapshot.children) {
                            val listData = i.getValue(DisplayUserData::class.java)
                            listData?.key = i.key.toString()

                            if (listData?.number == number) {
                                userKey = listData?.key
                            }

                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("onUser Database Failure", error.message )
                }
            })*/

        (FirebaseDatabase.getInstance().getReference("AllUser")).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (i in snapshot.children) {
                            val listData = i.getValue(DisplayAllUser::class.java)
                            listData?.key = i.key.toString()

                            if (listData?.number == number) {
                                Log.d("Listdata", listData.toString())
                                binding.edtUserName.setText(listData?.name)
                                key = listData?.key
                                binding.imgUserImage.setOnClickListener {
                                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                                    intent.type = "image/*"
                                    imagePicker.launch(intent)
                                }
                                binding.btnSaveInfo.setOnClickListener {
                                    updateUser()
                                    updateUI()

                                }
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("onCancelled:Database ", error.message)
                }
            }
        )

        binding.imgUserImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }

        binding.btnSaveInfo.setOnClickListener {
            newUser()
        }

    }

    private fun updateUser() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().getReference().child("AllUser")
            .child(key!!).child("name")
            .setValue(binding.edtUserName.text.toString()).addOnSuccessListener {

                FirebaseDatabase.getInstance().getReference().child("User")
                    .child(userID!!).child("name")
                    .setValue(binding.edtUserName.text.toString())
            }
        val user = FirebaseAuth.getInstance().currentUser?.phoneNumber
        storage.child("Users/Profile_Pictures/$user/$userID")
            .putFile(imageUri!!)
    }

    private fun updateUI() {
        requireActivity().supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragment_container, MainTabLayout())
                commit()
            }
    }

    private fun newUser() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        val data = SendUserData(
            binding.edtUserName.text.toString(),
            number!!,
            userID!!
        )
        val allUserData = SendAllUserData(
            FirebaseAuth.getInstance().currentUser?.uid!!,
            binding.edtUserName.text.toString(),
            number!!,
            userID
        )
        val user = FirebaseAuth.getInstance().currentUser?.phoneNumber
        storage.child("Users/Profile_Pictures/$user/$userID")
            .putFile(imageUri!!).apply {
                addOnSuccessListener {
                    database.child("AllUser").push().setValue(allUserData)
                    database.child("User/$userID").setValue(data)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .apply {
                            replace(R.id.fragment_container, MainTabLayout())
                            commit()
                        }
                }
                addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Storage reference Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private val imagePicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            imageUri = result.data?.data!!
            binding.imgUserImage.setImageURI(imageUri)
        }
    /*@SuppressLint("Range")
    private fun getFileName(fileUri: Uri?): String {
        var fileName: String? = null
        if (fileUri!!.scheme.equals("content")) {
            val cursor = requireActivity().contentResolver.query(fileUri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (fileName == null) {
            fileName = fileUri.path
            val cut = fileName!!.lastIndexOf('/')
            if (cut != -1) {
                fileName = fileName!!.substring(cut + 1)
            }
        }
        return fileName!!
    }*/


}