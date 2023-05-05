package com.example.chattingapp

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.chattingapp.databinding.FragmentProfileSettingBinding
import com.example.chattingapp.model.DisplayUserData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File


class ProfileSetting : Fragment() {

    lateinit var binding: FragmentProfileSettingBinding
    lateinit var storage: StorageReference
    lateinit var database: DatabaseReference
    lateinit var photo: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentProfileSettingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileToolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, MainTabLayout())
                commit()
            }
        }

        storage = Firebase.storage.reference
        database = Firebase.database.reference

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        database.child("User/$userId").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d("Profile Snapshot", snapshot.toString())
                        val listData = snapshot.getValue(DisplayUserData::class.java)!!
                        binding.txtUserName.text = listData.name
                        val userNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
                        binding.txtUserNumber.text = userNumber
                        val LocalFile = File.createTempFile("tempfile", "jpeg")
                        storage.child("Users/Profile_Pictures/$userNumber/$userId")
                            .getFile(LocalFile).addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                                binding.imgUserProfilePic.setImageBitmap(bitmap)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseError", error.message)

                }
            }
        )


        binding.imgEditUserProfilePic.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
            val viewLayout = layoutInflater.inflate(R.layout.profile_bottom_layout, null)
            bottomSheetDialog.setContentView(viewLayout)
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetDialog.show()

            viewLayout.findViewById<ImageView>(R.id.imgCameraImage).setOnClickListener {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, 1)
                bottomSheetDialog.dismiss()
            }
            viewLayout.findViewById<ImageView>(R.id.imgGalleryImage).setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                imagePicker.launch(intent)
                bottomSheetDialog.dismiss()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            photo = data!!.extras!!["data"] as Bitmap
            binding.imgUserProfilePic.setImageBitmap(photo)
            binding.imgUserProfilePic.isDrawingCacheEnabled = true
            binding.imgUserProfilePic.buildDrawingCache()

            val bitmap = (binding.imgUserProfilePic.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            val byteArray = baos.toByteArray()
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val userNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
            storage.child("Users/Profile_Pictures/$userNumber/$userId").putBytes(byteArray)
        }
    }

    private var imagePicker: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val imageUri = it.data?.data
            binding.imgUserProfilePic.setImageURI(imageUri)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val userNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
            storage.child("Users/Profile_Pictures/$userNumber/$userId").putFile(imageUri!!)
        }
}