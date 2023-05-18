package com.example.chattingapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils.replace
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.adapter.recentStatusAdapter
import com.example.chattingapp.adapter.viewedStatusAdpater
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
import kotlin.collections.HashMap

class Status : Fragment() {

    lateinit var binding: FragmentStatusBinding
    lateinit var storage: StorageReference
    private var usersArray = arrayListOf<DisplayAllUser>()
    private var UserStatus = arrayListOf<StatusModel>()
    private var userData: DisplayUserData? = null
    lateinit var recentstatusAdapter: recentStatusAdapter
    lateinit var viewedStatusAdapter: viewedStatusAdpater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storage = Firebase.storage.reference

        val userNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance().getReference("Status/$userId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (i in snapshot.children) {
                            binding.myStatusUploadLayout.visibility = View.GONE
                            binding.myStatusViewLayout.visibility = View.VISIBLE
                            val listData = i.getValue(StatusModel::class.java)
                            val LocalFile = File.createTempFile("tempfile", "jpeg")
                            FirebaseStorage.getInstance()
                                .getReference("Users/Status/${listData?.userId}/${listData?.statusId}")
                                .getFile(LocalFile).addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                                binding.imgMyStatusProfilePic.setImageBitmap(bitmap)
                            }

                            binding.myStatusViewLayout.setOnClickListener{
                                requireActivity().supportFragmentManager.beginTransaction().apply {
                                    replace(R.id.fragment_container, ViewStatus(listData!!))
                                    addToBackStack(null)
                                    commit()
                                }
                            }

                            binding.myStatusViewLayout.setOnLongClickListener {
                                val alertDialog = AlertDialog.Builder(context)
                                alertDialog.setTitle("Delete the Status")
                                alertDialog.setMessage("Are you sure ?")
                                alertDialog.setPositiveButton("Delete") { dialog, id ->
                                    FirebaseStorage.getInstance().getReference("Users/Status/${listData?.userId}/${listData?.statusId}").delete().addOnSuccessListener {
                                        FirebaseDatabase.getInstance().getReference("Status/${listData?.userId}/${listData?.statusId}").removeValue().addOnSuccessListener {
                                            Toast.makeText(requireContext(), "Status Deleted ", Toast.LENGTH_SHORT).show()
                                            dialog.dismiss()
                                        }
                                    }
                                }
                                alertDialog.setNegativeButton("Cancel"){ dialog , id ->
                                    dialog.dismiss()
                                }
                                alertDialog.show()
                                true
                            }
                        }
                    } else {
                        binding.myStatusViewLayout.visibility = View.GONE
                        binding.myStatusUploadLayout.visibility = View.VISIBLE
                        val LocalFile = File.createTempFile("tempfile", "jpeg")
                        storage.child("Users/Profile_Pictures/$userNumber/$userId")
                            .getFile(LocalFile)
                            .addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                                binding.imgUserImage.setImageBitmap(bitmap)
                            }
                        binding.myStatusUploadLayout.setOnClickListener {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent, 1)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


        binding.btnAddStatus.setOnClickListener {
            Toast.makeText(requireContext(), "Camera", Toast.LENGTH_SHORT).show()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1)
        }



        usersArray = arrayListOf()
        FirebaseDatabase.getInstance().getReference("User/$userId/personalChat")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (i in snapshot.children) {
                            val listData = i.getValue(DisplayAllUser::class.java)
                            listData?.key = i.key.toString()
                            usersArray.add(listData!!)
                        }

                        for (j in usersArray) {
                            FirebaseDatabase.getInstance().getReference("Status/${j.userID}")
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            for (k in snapshot.children) {
                                                val listStatus = k.getValue(StatusModel::class.java)
                                                UserStatus.add(listStatus!!)
                                                recentstatusAdapter.notifyDataSetChanged()
                                                viewedStatusAdapter.notifyDataSetChanged()
                                                Log.d("UserStatus", UserStatus.toString())
                                                break
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.d("Status Array", error.message)
                                    }
                                })
                        }

                        viewedStatusAdapter = viewedStatusAdpater(UserStatus,
                            object : viewedStatusAdpater.onClickedStatus {
                                override fun openStatus(statusModel: StatusModel) {
                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .apply {
                                            replace(
                                                R.id.fragment_container,
                                                ViewStatus(statusModel)
                                            )
                                            addToBackStack(null)
                                            commit()
                                        }
                                }
                            })
                        binding.recyclerViewedStatus.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        viewedStatusAdapter.notifyDataSetChanged()
                        binding.recyclerViewedStatus.adapter = viewedStatusAdapter

                        recentstatusAdapter = recentStatusAdapter(
                            UserStatus,
                            object : recentStatusAdapter.onClickedStatus {
                                override fun openStatus(statusModel: StatusModel) {
                                    UserStatus.clear()
                                    usersArray.clear()
                                    recentstatusAdapter.notifyDataSetChanged()
                                    viewedStatusAdapter.notifyDataSetChanged()

                                    val status = HashMap<String, Any>()
                                    status.put(userId!!, true)
                                    FirebaseDatabase.getInstance()
                                        .getReference("Status/${statusModel.userId}/${statusModel.statusId}")
                                        .child("viewStatus")
                                        .updateChildren(status).addOnSuccessListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Status Viewed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .apply {
                                            replace(
                                                R.id.fragment_container,
                                                ViewStatus(statusModel)
                                            )
                                            addToBackStack(null)
                                            commit()
                                        }
                                }
                            })

                        recentstatusAdapter.notifyDataSetChanged()
                        binding.recyclerRecentStatus.layoutManager =
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        binding.recyclerRecentStatus.adapter = recentstatusAdapter

                    } else {
                        binding.txtNoStatus.visibility = View.VISIBLE
                        binding.txtNoStatus.text = "No Status"

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        FirebaseDatabase.getInstance().getReference("User/$userId").get().addOnSuccessListener {
            userData = it.getValue(DisplayUserData::class.java)
        }

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
                        SimpleDateFormat("dd/MM/yyyy hh:mm a").format(Calendar.getInstance().time),
                        userData!!.name,
                        userId!!,
                        FirebaseAuth.getInstance().currentUser?.phoneNumber!!,

                        )
                    FirebaseDatabase.getInstance().getReference("Status/$userId")
                        .child(pushId).setValue(statusData).addOnSuccessListener {
                            val status = HashMap<String, Any>()
                            status.put(userId!!, false)
                            FirebaseDatabase.getInstance()
                                .getReference("Status/${userId}/${pushId}/viewStatus")
                                .updateChildren(status)
                            Toast.makeText(requireContext(), "Status Uploaded", Toast.LENGTH_SHORT)
                                .show()

                            recentstatusAdapter.notifyDataSetChanged()
                            viewedStatusAdapter.notifyDataSetChanged()
                        }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        UserStatus.clear()
        usersArray.clear()
        recentstatusAdapter.notifyDataSetChanged()
        viewedStatusAdapter.notifyDataSetChanged()
    }
}

/*

viewedStatusAdapter.notifyDataSetChanged()
binding.recyclerViewedStatus.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
binding.recyclerViewedStatus.adapter = viewedStatusAdapter
*/
