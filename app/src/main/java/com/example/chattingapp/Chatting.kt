package com.example.chattingapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chattingapp.adapter.ChatAdapter
import com.example.chattingapp.databinding.FragmentChattingBinding
import com.example.chattingapp.model.DisplayAllUser
import com.example.chattingapp.model.MessageData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Chatting(val displayAllUser: DisplayAllUser) : Fragment() {

    lateinit var binding: FragmentChattingBinding
    lateinit var database: DatabaseReference
    private var senderRoom = ""
    private var receiverRoom = ""
    lateinit var chatAdapter: ChatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChattingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = Firebase.database.reference
        senderRoom = FirebaseAuth.getInstance().currentUser!!.uid
        receiverRoom = displayAllUser.userID

        val senderId = FirebaseAuth.getInstance().currentUser?.uid
        val receiverId = displayAllUser.userID
        senderRoom = senderId + receiverId
        receiverRoom = receiverId + senderId
        binding.toolbar.title = displayAllUser.name
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, HomePage())
                commit()
            }
        }

        binding.btnSendMsg.setOnClickListener {
            val msg = MessageData(
                userId!!,
                binding.editInputMsg.text.toString()
            )

            Log.d("msg", msg.toString())
            database.child("ChatRoom/$senderRoom").child("Message").push().setValue(msg)
                .addOnSuccessListener {
                    database.child("ChatRoom/$receiverRoom").child("Message").push().setValue(msg)
                }
            binding.editInputMsg.setText("")

        }
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.stackFromEnd = true

        val messageArray = arrayListOf<MessageData>()
        chatAdapter = ChatAdapter(messageArray)
        FirebaseDatabase.getInstance().getReference("ChatRoom/$senderRoom/Message")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messageArray.clear()
                        if (snapshot.exists()) {
                            for (i in snapshot.children) {
                                val getMessage = i.getValue(MessageData::class.java)
                                messageArray.add(getMessage!!)
                            }
                            chatAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                }
            )

        binding.recyclerViewChatting.layoutManager = linearLayoutManager
        binding.recyclerViewChatting.adapter = chatAdapter
    }
}