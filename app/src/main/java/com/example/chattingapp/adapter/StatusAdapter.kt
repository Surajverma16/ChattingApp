package com.example.chattingapp.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.R
import com.example.chattingapp.model.StatusModel
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class StatusAdapter(val statusArray: ArrayList<StatusModel>, val clicked : onClickedStatus) :
    RecyclerView.Adapter<StatusAdapter.statusViewHolder>() {
    class statusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusImage = itemView.findViewById<ImageView>(R.id.imgStatusUsersProfilePic)
        val statusName = itemView.findViewById<TextView>(R.id.txtStatusUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): statusViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_status, parent, false)
        return statusViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return statusArray.size
    }

    override fun onBindViewHolder(holder: statusViewHolder, position: Int) {
        holder.statusName.text = statusArray[position].userName
        val LocalFile = File.createTempFile("tempfile", "jpeg")
        FirebaseStorage.getInstance()
            .getReference("Users/Profile_Pictures/${statusArray[position].userNumber}/${statusArray[position].userId}")
            .getFile(LocalFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(LocalFile.absolutePath)
                holder.statusImage.setImageBitmap(bitmap)
            }

        holder.itemView.setOnClickListener {
            clicked.openStatus(statusArray[position])
        }
    }
    interface onClickedStatus{
        fun openStatus(statusModel: StatusModel)
    }
}