package com.example.chattingapp

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.adapter.SearchUserAdapter
import com.example.chattingapp.adapter.UserAdapter
import com.example.chattingapp.authorization.SignInPage
import com.example.chattingapp.databinding.FragmentHomePageBinding
import com.example.chattingapp.model.DisplayAllUser
import com.example.chattingapp.model.SendAllUserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class HomePage : Fragment() {

    lateinit var binding: FragmentHomePageBinding
    lateinit var auth: FirebaseAuth
    lateinit var database: DatabaseReference
    private var listUser = arrayListOf<DisplayAllUser>()
    private var searchUser = arrayListOf<DisplayAllUser>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomePageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        database = Firebase.database.reference

        val number = FirebaseAuth.getInstance().currentUser?.phoneNumber
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val LocalFile = File.createTempFile("tempfile", "jpeg")
        FirebaseStorage.getInstance()
            .getReference("Users/Profile_Pictures/+91$number}/$userId}")
            .getFile(LocalFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
            }

        database.child("User/$userId/personalChat")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listUser = arrayListOf()
                    if (snapshot.exists()) {
                        for (i in snapshot.children) {
                            val listData = i.getValue(DisplayAllUser::class.java)
                            listData?.key = i.key.toString()

                            if (listData?.userID != userId) {
                                listUser.add(listData!!)
                            }

                            binding.recyclerViewUserList.layoutManager = LinearLayoutManager(
                                context?.applicationContext,
                                LinearLayoutManager.VERTICAL,
                                false
                            )

                            binding.recyclerViewUserList.adapter =
                                UserAdapter(listUser, object : UserAdapter.onChatClicked {
                                    override fun getChat(displayAllUser: DisplayAllUser) {
                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .apply {
                                                replace(
                                                    R.id.fragment_container,
                                                    Chatting(displayAllUser)
                                                )
                                                addToBackStack("MainTabLayout")
                                                commit()
                                            }
                                    }

                                    override fun openImage(displayAllUser: DisplayAllUser) {
                                        val alertDialog = Dialog(requireContext())
                                        val profileView = layoutInflater.inflate(
                                            R.layout.open_profile_image,
                                            null
                                        )
                                        profileView.findViewById<TextView>(R.id.txtOpenProfileName).text =
                                            displayAllUser.name

                                        val image =
                                            profileView.findViewById<ImageView>(R.id.imgOpenProfile)
                                        alertDialog.setContentView(profileView)
                                        alertDialog.show()
                                        profileView.findViewById<ImageView>(R.id.imgProfileMessage)
                                            .setOnClickListener {
                                                requireActivity().supportFragmentManager.beginTransaction()
                                                    .apply {
                                                        replace(
                                                            R.id.fragment_container,
                                                            Chatting(displayAllUser)
                                                        )
                                                        addToBackStack("MainTabLayout")
                                                        commit()
                                                        alertDialog.dismiss()
                                                    }
                                            }

                                        profileView.findViewById<ImageView>(R.id.imgProfileInfo)
                                            .setOnClickListener {
                                                requireActivity().supportFragmentManager.beginTransaction()
                                                    .apply {
                                                        replace(
                                                            R.id.fragment_container,
                                                            ViewUsersProfile(displayAllUser)
                                                        )
                                                        addToBackStack("MainTabLayout")
                                                        commit()
                                                        alertDialog.dismiss()
                                                    }
                                            }

                                        val LocalFile = File.createTempFile("tempfile", "jpeg")
                                        FirebaseStorage.getInstance()
                                            .getReference("Users/Profile_Pictures/+91${displayAllUser.number}/${displayAllUser.userID}")
                                            .getFile(LocalFile).addOnSuccessListener {
                                                val bitmap =
                                                    BitmapFactory.decodeFile(LocalFile.absolutePath)
                                                image.setImageBitmap(bitmap)
                                                image.setOnClickListener {
                                                    alertDialog.dismiss()
                                                    requireActivity().supportFragmentManager.beginTransaction()
                                                        .apply {
                                                            replace(
                                                                R.id.fragment_container,
                                                                ViewImage(displayAllUser)
                                                            )
                                                            addToBackStack("MainTabLayout")
                                                            commit()
                                                        }
                                                }
                                            }
                                    }
                                })
                        }
                    } else {
                        binding.txtNoChat.text = "No Recent Chat Available"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Database Failure Home", error.message)
                }
            })


        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {
                val userToken = HashMap<String, Any>()
                userToken.put("token", it.toString())

                val userID = FirebaseAuth.getInstance().currentUser?.uid
                FirebaseDatabase.getInstance().getReference("User/$userID")
                    .updateChildren(userToken)
            }

        binding.btnAddUser.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext()).create()
            val addUserView = layoutInflater.inflate(R.layout.add_user_layout, null)
            alertDialog.setView(addUserView)
            alertDialog.show()
            val edtSearchUser = addUserView.findViewById<EditText>(R.id.edtSearchInput)
            val recyclerViewAddUserList =
                addUserView.findViewById<RecyclerView>(R.id.recyclerViewAddUserList)
            val btnSearchUser = addUserView.findViewById<Button>(R.id.btnSearchUser)

            btnSearchUser.setOnClickListener {
                if (edtSearchUser.text.isNotEmpty() && edtSearchUser.length() == 10) {
                    val userNumber = edtSearchUser.text.toString()
                    database.child("AllUser").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists())
                                for (i in snapshot.children) {
                                    searchUser = arrayListOf()
                                    val listData = i.getValue(DisplayAllUser::class.java)
                                    if (listData?.number.equals(userNumber)) {
                                        var isAddUser = true
                                        for (i in listUser) {
                                            if (i.number.equals(userNumber)) {
                                                isAddUser = false
                                            }
                                            val currentUserNumber =
                                                FirebaseAuth.getInstance().currentUser?.phoneNumber
                                            currentUserNumber?.replace("+91", null.toString())
                                            if (i.number.equals(currentUserNumber)) {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Can't Add yourself",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                isAddUser = false
                                            }
                                        }
                                        if (isAddUser) {
                                            searchUser.add(listData!!)
                                            recyclerViewAddUserList.layoutManager =
                                                LinearLayoutManager(
                                                    requireContext(),
                                                    LinearLayoutManager.VERTICAL,
                                                    false
                                                )
                                            recyclerViewAddUserList.adapter = SearchUserAdapter(
                                                searchUser,
                                                object : SearchUserAdapter.addUser {
                                                    override fun onUserAdd(displayAllUser: DisplayAllUser) {
                                                        val userID =
                                                            FirebaseAuth.getInstance().currentUser?.uid
                                                        val sendUserData = SendAllUserData(
                                                            displayAllUser.userID,
                                                            displayAllUser.name,
                                                            displayAllUser.number,
                                                            displayAllUser.profileImgName
                                                        )
                                                        database.child("User/$userID/personalChat")
                                                            .push().setValue(sendUserData)
                                                        alertDialog.dismiss()
                                                    }
                                                })
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "already Added",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("DatabaseError", error.message)
                        }
                    })

                } else if (edtSearchUser.text.isEmpty()) {
                    Toast.makeText(requireContext(), "Please Enter number", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please enter a valid Number",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


}
