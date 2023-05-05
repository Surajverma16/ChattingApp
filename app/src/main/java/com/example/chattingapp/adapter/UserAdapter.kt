package com.example.chattingapp.adapter

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.MainActivity
import com.example.chattingapp.R
import com.example.chattingapp.ViewImage
import com.example.chattingapp.model.DeleteMessageData
import com.example.chattingapp.model.DisplayAllUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class UserAdapter(val user: ArrayList<DisplayAllUser>, private val clickedChat: onChatClicked) :
    RecyclerView.Adapter<UserAdapter.userViewHolder>() {
    class userViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName = view.findViewById<TextView>(R.id.txtUserName)
        val userPic = view.findViewById<ImageView>(R.id.imgProfilePic)
        val lastMessage = view.findViewById<TextView>(R.id.txtLastMessage)
        val dateTime = view.findViewById<TextView>(R.id.txtMessageTimeAndDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): userViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_user, parent, false)
        return userViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return user.size
    }

    override fun onBindViewHolder(holder: userViewHolder, position: Int) {
        holder.userName.text = user[position].name
        val LocalFile = File.createTempFile("tempfile", "jpeg")
        FirebaseStorage.getInstance()
            .getReference("Users/Profile_Pictures/+91${user[position].number}/${user[position].profileImgName}")
            .getFile(LocalFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                holder.userPic.setImageBitmap(bitmap)
            }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseDatabase.getInstance()
            .getReference("ChatRoom/${user[position].userID + userId}/Message")
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (i in snapshot.children) {
                                Log.d("User snapshot", i.toString())
                                val listData = i.getValue(DeleteMessageData::class.java)
                                listData?.key = i.key.toString()
                                Log.d("chat listData", listData.toString())
                                if (listData?.imageUri != "") {
                                    holder.lastMessage.text = "Photo"
                                    holder.dateTime.text = listData?.messageTime
                                } else if (listData.fileUrl != "") {
                                    holder.lastMessage.text = "File"
                                    holder.dateTime.text = listData?.messageTime
                                } else if (listData.message != "") {
                                    holder.lastMessage.text = listData?.message
                                    holder.dateTime.text = listData?.messageTime
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("LastMessage Error", error.message)
                    }
                }
            )
        holder.userPic.setOnClickListener {
            clickedChat.openImage(user[position])
        }
        holder.itemView.setOnClickListener {
            clickedChat.getChat(user[position])
        }
    }

    interface onChatClicked {
        fun getChat(displayAllUser: DisplayAllUser)
        fun openImage(displayAllUser: DisplayAllUser)
    }
}