package com.example.chattingapp

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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
                                                addToBackStack(this@HomePage.toString())
                                                commit()
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

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.account -> true
                R.id.signOut -> {
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

        binding.btnAddUser.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext()).create()
            val addUserView = layoutInflater.inflate(R.layout.add_user_layout, null)
            alertDialog.setView(addUserView)
            alertDialog.show()
            val edtSearchUser = addUserView.findViewById<EditText>(R.id.edtSearchInput)
            val recyclerViewAddUserlist =
                addUserView.findViewById<RecyclerView>(R.id.recyclerViewAddUserList)
            val btnAddUser = addUserView.findViewById<Button>(R.id.btnSearchUser)

            btnAddUser.setOnClickListener {
                if (edtSearchUser.text.isNotEmpty() && edtSearchUser.length() == 10) {
                    val userNumber = edtSearchUser.text.toString()
                    database.child("AllUser").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists())
                                for (i in snapshot.children) {
                                    searchUser = arrayListOf()
                                    val listData = i.getValue(DisplayAllUser::class.java)
                                    if (listData?.number.equals(userNumber)) {
                                        searchUser.add(listData!!)
                                        recyclerViewAddUserlist.layoutManager = LinearLayoutManager(
                                            requireContext(),
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )
                                        recyclerViewAddUserlist.adapter = SearchUserAdapter(
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
                                    }
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
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