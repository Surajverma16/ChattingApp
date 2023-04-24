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

class SearchUserAdapter(val userList : ArrayList<DisplayAllUser>, val clicked : addUser):RecyclerView.Adapter<SearchUserAdapter.searchUserViewHolder>() {
    class searchUserViewHolder(itemView : View):RecyclerView.ViewHolder(itemView) {
        val userName = itemView.findViewById<TextView>(R.id.txtSearchUserName)
        val userNumber = itemView.findViewById<TextView>(R.id.txtUserNumber)
        val addUser = itemView.findViewById<ImageView>(R.id.imgAddUserToChat)
        val userProfile = itemView.findViewById<ImageView>(R.id.imgUserProfilePic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): searchUserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_search_user, parent, false)
        return searchUserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
       return userList.size
    }

    override fun onBindViewHolder(holder: searchUserViewHolder, position: Int) {
        holder.userName.text = userList[position].name
        holder.userNumber.text = userList[position].number
        val LocalFile = File.createTempFile("tempfile", "jpeg")
        FirebaseStorage.getInstance()
            .getReference("Users/Profile_Pictures/+91${userList[position].number}/${userList[position].profileImgName}")
            .getFile(LocalFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                holder.userProfile.setImageBitmap(bitmap)
            }
        holder.addUser.setOnClickListener {
            clicked.onUserAdd(userList[position])
        }

    }
    interface addUser{
        fun onUserAdd(displayAllUser: DisplayAllUser)
    }
}