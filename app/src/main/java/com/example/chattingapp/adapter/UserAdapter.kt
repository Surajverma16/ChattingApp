package com.example.chattingapp.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.R
import com.example.chattingapp.model.DisplayAllUser
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class UserAdapter (val user : ArrayList<DisplayAllUser>, val clickedChat : onChatClicked): RecyclerView.Adapter<UserAdapter.userViewHolder>() {
    class userViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName = view.findViewById<TextView>(R.id.txtUserName)
        val userPic = view.findViewById<ImageView>(R.id.imgProfilePic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): userViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_user,parent,false)
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
        holder.itemView.setOnClickListener {
            clickedChat.getChat(user[position])
        }
    }

    interface onChatClicked{
        fun getChat(displayAllUser: DisplayAllUser)
    }
}